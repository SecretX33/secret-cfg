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
package com.github.secretx33.secretcfg.core.util

import java.util.function.Predicate

/**
 * Predicates used as default to always accept or refuse item.
 *
 * @since 1.0
 */
object Predicates {

    private val ALWAYS_TRUE = object : Predicate<Any?> {
        override fun test(t: Any?): Boolean = true

        override fun and(other: Predicate<Any?>): Predicate<Any?> = other

        override fun or(other: Predicate<Any?>): Predicate<Any?> = this

        override fun negate(): Predicate<Any?> = refuse()
    }

    private val ALWAYS_FALSE = object : Predicate<Any?> {
        override fun test(t: Any?): Boolean = false

        override fun and(other: Predicate<Any?>): Predicate<Any?> = this

        override fun or(other: Predicate<Any?>): Predicate<Any?> = other

        override fun negate(): Predicate<Any?> = accept()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> accept(): Predicate<T> = ALWAYS_TRUE as Predicate<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> refuse(): Predicate<T> = ALWAYS_FALSE as Predicate<T>
}
