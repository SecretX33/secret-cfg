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

import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

/**
 * A simple expiring set implementation using Guava cache
 *
 * @param E : Any The set cache type
 * @param [duration] Long How long should each item be kept
 * @param [unit] TimeUnit The time unit of the duration parameter
 * @constructor
 */
class ExpiringSet<E : Any>(duration: Long, unit: TimeUnit) {

    private val cache = CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build<E, Long>()
    private val lifetime = unit.toMillis(duration)

    /**
     * Add the item to the set, returning if it was not present before
     *
     * @param item E The item to be added to the set
     * @return Boolean True if the item was not present before
     */
    fun add(item: E): Boolean {
        val present = contains(item)
        cache.put(item, System.currentTimeMillis() + lifetime)
        return !present
    }

    operator fun contains(item: E): Boolean {
        val timeout = cache.getIfPresent(item)
        return timeout != null && timeout > System.currentTimeMillis()
    }

    fun remove(item: E): Boolean {
        val present = contains(item)
        cache.invalidate(item)
        return present
    }
}

