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
 * A simple expiring map implementation using Guava cache
 *
 * @param K : Any The map cache key type
 * @param V: Any The map cache value type
 * @param [duration] Long How long should each item be kept
 * @param [unit] TimeUnit The time unit of the duration parameter
 * @constructor
 */
class ExpiringMap<K : Any, V: Any>(duration: Long, unit: TimeUnit) {

    private val cache = CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build<K, Pair<Long, V>>()
    private val lifetime = unit.toMillis(duration)

    /**
     * Add the key and value to the map, returning if it was not present before.
     *
     * @param key K The key to be added to the map
     * @param value V The value to be added to the map
     * @return Boolean True if the key with that value was not present before
     */
    fun put(key: K, value: V): Boolean {
        val present = contains(key, value)
        cache.put(key, Pair(System.currentTimeMillis() + lifetime, value))
        return !present
    }

    fun putAll(map: Map<K, V>) {
        cache.putAll(map.mapValues { Pair(System.currentTimeMillis() + lifetime, it.value) })
    }

    fun contains(key: K, value: V): Boolean {
        val timeout = cache.getIfPresent(key)
        return timeout != null && timeout.first > System.currentTimeMillis() && timeout.second == value
    }

    fun remove(key: K) {
        cache.invalidate(key)
    }
}


