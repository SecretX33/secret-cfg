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
package com.github.secretx33.secretcfg.core.util.extension

import java.nio.file.Path
import java.nio.file.Paths
import java.util.Collections
import kotlin.io.path.name
import kotlin.reflect.KClass

/**
 * Extension method to give generic enum classes access to the static method [Enum#values()][Enum.values()].
 *
 * @since 1.0
 */
fun <T : Enum<T>> KClass<out T>.values(): Array<out T> = java.enumConstants

internal fun Path.nameEndsWithAny(vararg others: String): Boolean = others.isEmpty() || others.any { name.endsWith(it, ignoreCase = true) }

internal fun String.toPath() = Paths.get(this)

internal fun <T> Set<T>.unmodifiable(): Set<T> = Collections.unmodifiableSet(this)
