/**
 * MIT License
 *
 * Copyright (c) 2021 SecretX33
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.secretx33.secretcfg.core.storage.filewatcher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.IOException
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystem
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Utility for "watching" for file changes using a [WatchService].
 *
 * @property autoRegisterNewSubDirectories Boolean If this file watcher should discover directories
 * @since 1.0
 */
abstract class AbstractFileWatcher (
    fileSystem: FileSystem,
    private val autoRegisterNewSubDirectories: Boolean,
) : AutoCloseable {
    /** The watch service  */
    private val watchService: WatchService = fileSystem.newWatchService()

    /** A map of all registered watch keys  */
    private val keys = ConcurrentHashMap<WatchKey, Path>()

    /** The coroutine currently being used to wait for & process watch events  */
    private val processingCoroutine = AtomicReference<Job?>()

    /**
     * The Coroutine Thread that will be used only to monitor changes of this folder
     */
    private val watcherThread = newSingleThreadContext("FileWatcherIoThread")

    /**
     * Register a watch key in the given directory.
     *
     * @param directory the directory
     * @throws IOException if unable to register a key
     */
    fun register(directory: Path) {
        val key = register(watchService, directory)
        keys[key] = directory
    }

    /**
     * Register a watch key recursively in the given directory.
     *
     * @param root the root directory
     * @throws IOException if unable to register a key
     */
    fun registerRecursively(root: Path) {
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                register(dir)
                return super.preVisitDirectory(dir, attrs)
            }
        })
    }

    /**
     * Process an observed watch event.
     *
     * @param event the event
     * @param filePath the resolved event context
     */
    protected abstract fun processEvent(event: FileWatcherEvent)

    /**
     * Processes [WatchEvent]s from the watch service until it is closed, or until
     * the thread is interrupted.
     */
    @Suppress("UNCHECKED_CAST")
    fun runEventProcessingLoop() {
        CoroutineScope(watcherThread).launch {
            check(processingCoroutine.compareAndSet(null, coroutineContext.job)) { "A coroutine is already processing events for this watcher." }

            while (isActive) {
                // poll for a key from the watch service
                val key: WatchKey = try {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    watchService.take()
                } catch (e: InterruptedException) {
                    break
                } catch (e: ClosedWatchServiceException) {
                    break
                }

                // find the directory the key is watching
                val directory = keys[key]
                if (directory == null) {
                    key.cancel()
                    continue
                }

                // process each watch event the key has
                key.pollEvents().asSequence().map { it as WatchEvent<Path> }.forEach { event ->
                    event.context()?.takeIf { it.nameCount > 0 }
                        ?.let { directory.resolve(it) }
                        ?.let { file ->
                            // if the file is a regular file, send the event on to be processed
                            if (Files.isRegularFile(file))
                                processEvent(FileWatcherEvent(file, event.getModificationType()))

                            // handle recursive directory creation
                            if (autoRegisterNewSubDirectories && event.kind() == ENTRY_CREATE) {
                                try {
                                    if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
                                        registerRecursively(file)
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                }

                // reset the key
                val valid = key.reset()
                if (!valid) keys.remove(key)
            }
            processingCoroutine.compareAndSet(coroutineContext.job, null)
        }
    }

    override fun close() {
        try {
            watchService.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Get a [WatchKey] from the given [WatchService] in the given [directory][Path].
         *
         * @param watchService the watch service
         * @param directory the directory
         * @return the watch key
         * @throws IOException if unable to register
         */
        private fun register(watchService: WatchService, directory: Path): WatchKey
            = directory.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
    }

    private fun WatchEvent<Path>.getModificationType() = when {
        kind() == ENTRY_CREATE -> FileModificationType.CREATE
        kind() == ENTRY_MODIFY -> FileModificationType.MODIFY
        kind() == ENTRY_DELETE -> FileModificationType.DELETE
        kind() == OVERFLOW -> FileModificationType.OVERFLOW
        else -> throw IllegalStateException("Could not convert unknown event $this to a FileModificationType")
    }
}
