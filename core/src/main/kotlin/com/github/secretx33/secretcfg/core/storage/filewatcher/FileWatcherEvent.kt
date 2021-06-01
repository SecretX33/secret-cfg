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
@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.secretx33.secretcfg.core.storage.filewatcher

import com.github.secretx33.secretcfg.core.util.extension.unmodifiable
import java.nio.file.Path
import java.util.EnumSet
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * Contains data about an event, what file was changed, what type of change was,
 * and two handy extension function to quickly get the filename.
 *
 * @property file Path The path to the modified file.
 * @property type FileModificationType
 * @property fileName String
 * @since 1.0
 */
data class FileWatcherEvent (
    val file: Path,
    val type: FileModificationType,
) {
    val fileName: String get() = file.name
    val fileNameWithoutExtension get() = file.nameWithoutExtension
}

/**
 * Represents all modifications that can happen on a file.
 *
 * @since 1.0
 */
enum class FileModificationType {
    /**
     * Triggered when a new entry is made in the watched directory. It could be due to the creation of a new file or renaming of an existing file.
     */
    CREATE,

    /**
     * Triggered when an existing entry in the watched directory is modified. All file edit's trigger this event. On some platforms, even changing file attributes will trigger it.
     */
    MODIFY,

    /**
     * Triggered when an entry is deleted, moved or renamed in the watched directory.
     */
    DELETE,

    /**
     * Triggered to indicate lost or discarded events. Unless you know you need this, you can safely ignore this entry
     */
    OVERFLOW;

    val isCreate get() = this == CREATE
    val isModify get() = this == MODIFY
    val isDelete get() = this == DELETE

    val isNotCreate get() = !isCreate
    val isNotModify get() = !isModify
    val isNotDelete get() = !isDelete

    val isCreateOrModify get() = isCreate || isModify
    val isNotCreateOrModify get() = !isCreateOrModify

    val isCreateOrDelete get() = isCreate || isDelete
    val isNotCreateOrDelete get() = !isCreateOrDelete

    companion object {
        val CREATE_AND_MODIFICATION: Set<FileModificationType> = EnumSet.of(CREATE, MODIFY).unmodifiable()
    }
}
