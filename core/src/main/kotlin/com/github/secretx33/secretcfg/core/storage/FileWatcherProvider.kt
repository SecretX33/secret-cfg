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
package com.github.secretx33.secretcfg.core.storage

import com.github.secretx33.secretcfg.core.storage.filewatcher.FileWatcher
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * Provider to cache created FileWatchers for multiple files.
 *
 * @since 1.0
 */
object FileWatcherProvider {

    /**
     * Map containing all created watchers for all folders (usually there'll be just one, monitoring the dataFolder)
     */
    private val watchers = ConcurrentHashMap<Path, FileWatcher>()

    /**
     * Gets a file watcher able to register listeners for file changes.
     *
     * @param basePath Path Base folder of the application that needs to be monitored, usually the DataFolder in case of Plugins
     * @param autoRegisterNewSubDirectories Boolean If new subfolders should be registered automatically
     * @return FileWatcher The file watcher responsible for that path
     */
    fun get(basePath: Path, autoRegisterNewSubDirectories: Boolean = true): FileWatcher
        = watchers.getOrPut(basePath) { FileWatcher(basePath, autoRegisterNewSubDirectories) }
}
