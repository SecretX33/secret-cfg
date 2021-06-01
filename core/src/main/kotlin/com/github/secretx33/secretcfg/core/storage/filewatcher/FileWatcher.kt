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

import com.github.secretx33.secretcfg.core.util.ExpiringMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.relativeTo

/**
 * Simple implementation of [AbstractFileWatcher] for Plugin data files.
 *
 * @property basePath Path The base watched path, needs o be absolute
 * @property autoRegisterNewSubDirectories Boolean If this file watcher should discover directories
 * @since 1.0
 */
class FileWatcher (
    basePath: Path,
    autoRegisterNewSubDirectories: Boolean = true,
) : AbstractFileWatcher(basePath.fileSystem, autoRegisterNewSubDirectories) {

    private val basePath: Path = basePath.toAbsolutePath()

    /** A map of watched locations with corresponding listeners  */
    private val watchedLocations = ConcurrentHashMap<Path, WatchedLocation>()

    init {
        if(!this.basePath.exists()) basePath.toFile().mkdirs()
        require(this.basePath.exists()) { "basePath needs to exist, current it doesn't" }
        require(this.basePath.isDirectory()) { "basePath needs to be a directory, but it isn't" }
        require(this.basePath.isAbsolute) { "basePath needs to be absolute" }
        super.registerRecursively(this.basePath)
        super.runEventProcessingLoop()
    }

    /**
     * Gets a [WatchedLocation] instance for a given path.
     *
     * @param path Path The path to get a watcher for
     * @return WatchedLocation The watched location
     */
    fun getWatcher(path: Path): WatchedLocation {
        val relativePath = when {
            path.isAbsolute -> path.relativeTo(basePath)
            path.startsWith(basePath) -> path
            else -> basePath.resolve(path).relativeTo(basePath)
        }
        return watchedLocations.getOrPut(relativePath) { WatchedLocation(basePath) }
    }

    /**
     * Gets a [WatchedLocation] instance for a given path.
     *
     * @param path Path the path to get a watcher for
     * @param listener suspend (FileWatcherEvent) -> Unit A shortcut to add a listener for when a file is updated
     * @return WatchedLocation the watched location
     */
    fun getWatcher(path: Path, modificationTypes: Set<FileModificationType>, listener: suspend (FileWatcherEvent) -> Unit): WatchedLocation
            = getWatcher(path).apply { addListener(modificationTypes, listener) }

    /**
     * Gets a [WatchedLocation] instance for the base path.
     *
     * @return WatchedLocation The watcher for the base path
     */
    fun getRootWatcher(): WatchedLocation = getWatcher(basePath)

    fun getRootWatcher(modificationTypes: Set<FileModificationType>, listener: suspend (FileWatcherEvent) -> Unit): WatchedLocation
        = getRootWatcher().apply { addListener(modificationTypes, listener) }

    /**
     * Gets a [WatchedLocation] instance for the given path
     *
     * @param path String A path relative to the already provided basePath
     * @return WatchedLocation
     */
    fun getWatcher(path: String): WatchedLocation = getWatcher(Path(path))

    fun getWatcher(path: String, modificationTypes: Set<FileModificationType>, listener: suspend (FileWatcherEvent) -> Unit): WatchedLocation
        = getWatcher(path).apply { addListener(modificationTypes, listener) }

    override fun processEvent(event: FileWatcherEvent) {
        // return if there's no element
        if(event.file.nameCount == 0) return

        // pass the event to all consumers that match
        watchedLocations.filter { event.file.startsWith(basePath.resolve(it.key).absolute()) }
            .forEach { (_, value) -> value.onEvent(event) }
    }

    /**
     * Encapsulates a "watcher" in a specific directory.
     *
     * @property path Path The directory or file being watched by this instance.
     */
    class WatchedLocation internal constructor(private val basePath: Path) {

        /** A set of files which have been modified recently  */
        private val recentlyConsumedFiles: ExpiringMap<UUID, Path> = ExpiringMap(1, TimeUnit.SECONDS)

        /**
         * The listener callback functions.
         * Path is the relative path of the file, from the basePath perspective
         */
        private val callbacks: MutableList<FileWatcherEventConsumer> = CopyOnWriteArrayList()

        /**
         * Triggered when a file is modified.
         *
         * @param event FileWatcherEvent The event containing information about the file being modified
         */
        fun onEvent(event: FileWatcherEvent) {
            // pass the event onto registered listeners
            callbacks.asSequence()
                .filter { it.acceptTypes.contains(event.type) && !recentlyConsumedFiles.put(it.uniqueId, event.file.relativeTo(basePath)) }
                .forEach {
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            it.listener(event)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
        }

        /**
         * Record that a file has been changed recently.
         *
         * @param fileName the name of the file
         */
        fun recordChange(path: Path) {
            callbacks.associate { it.uniqueId to path }.let { recentlyConsumedFiles.putAll(it) }
        }

        /**
         * Register a listener.
         *
         * @param listener the listener
         */
        fun addListener(modificationTypes: Set<FileModificationType>, listener: suspend (FileWatcherEvent) -> Unit) {
            require(modificationTypes.isNotEmpty()) { "You cannot register a listener that doesn't listen to any modifications" }

            callbacks.add(FileWatcherEventConsumer(listener, modificationTypes.asEnumSet()))
        }

        private fun Set<FileModificationType>.asEnumSet() = this as? EnumSet<FileModificationType> ?: EnumSet.copyOf(this)
    }
}

