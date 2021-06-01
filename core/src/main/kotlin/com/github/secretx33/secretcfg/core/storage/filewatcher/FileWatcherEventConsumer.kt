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

import java.util.UUID

/**
 * Represents a consumer and what type of modifications it want to listen for.
 *
 * @property listener Consumer<FileWatcherEvent> The consumer to be ran when an event happens
 * @property acceptTypes Set<FileModificationType> What types of modification does this consumer wants to consume
 * @property uniqueId UUID A random, unique identifier to distinguish between listeners
 * @since 1.0
 */
internal data class FileWatcherEventConsumer (
    val listener: suspend (FileWatcherEvent) -> Unit,
    val acceptTypes: Set<FileModificationType>,
) {
    val uniqueId: UUID = UUID.randomUUID()

    init { require(acceptTypes.isNotEmpty()) { "acceptTypes cannot be empty" } }
}
