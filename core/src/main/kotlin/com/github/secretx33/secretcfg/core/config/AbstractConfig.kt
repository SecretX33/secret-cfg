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
package com.github.secretx33.secretcfg.core.config

import com.github.secretx33.secretcfg.core.exception.DifferentCachedTypeException
import com.github.secretx33.secretcfg.core.manager.YamlManager
import com.github.secretx33.secretcfg.core.utils.RangeParser
import com.github.secretx33.secretcfg.core.utils.values
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

open class AbstractConfig (
    plugin: Any,
    dataFolder: File,
    path: String,
    protected val logger: Logger,
    copyDefault: Boolean,
    filePresentInJar: Boolean,
) : BaseConfig {

    protected val manager = YamlManager(plugin, dataFolder, path, logger, copyDefault, filePresentInJar)
    protected val cache = ConcurrentHashMap<String, Any>()

    override fun reload() {
        manager.reload()
        cache.clear()
    }

    override fun has(key: String): Boolean = manager.contains(key)

    override fun contains(key: String): Boolean = has(key)

    override fun set(key: String, value: Any) {
        manager.set(key, value)
        cache[key] = value
    }

    override fun setBoolean(key: String, value: Boolean) {
        manager.setBoolean(key, value)
        cache[key] = value
    }

    override fun setInt(key: String, value: Int) {
        manager.setInt(key, value)
        cache[key] = value
    }

    override fun setDouble(key: String, value: Double) {
        manager.setDouble(key, value)
        cache[key] = value
    }

    override fun setString(key: String, value: String) {
        manager.setString(key, value)
        cache[key] = value
    }

    override fun save() = manager.save()

    override val file: String
        get() = manager.fileName

    override val fileWithoutExtension: String
        get() = manager.file.nameWithoutExtension

    override val path: String
        get() = manager.relativePath

    override fun getKeys(): Set<String> = manager.getKeys()

    override fun getKeys(path: String): Set<String> = manager.getKeys(path)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: String, default: T): T {
        return cache.getOrPut(key) {
            runCatching { manager.get(key) as T }.getOrNull() ?: run {
                warnWrongType(key, manager.get(key), default)
                default
            }
        }.let { runCatching { it as T }.getOrElse { cacheException(key, it, default) } }
    }

    override fun getInt(key: String, default: Int, minValue: Int, maxValue: Int): Int
        = cachedAny(key, default) { (manager.get(key) as? Int)?.let { int -> max(minValue, min(maxValue, int)) } }

    override fun getFloat(key: String, default: Float, minValue: Float, maxValue: Float): Float
        = cachedAny(key, default) { (manager.get(key, default) as? Float)?.let { float -> max(minValue, min(maxValue, float)) } }

    override fun getDouble(key: String, default: Double, minValue: Double, maxValue: Double): Double
        = cachedAny(key, default) { (manager.get(key, default) as? Double)?.let { double -> max(minValue, min(maxValue, double)) } }

    override fun getString(key: String, default: String): String
        = cache.getOrPut(key) { manager.getString(key, default) }
            .let { runCatching { it as String }.getOrElse { cacheException(key, it, default) } }

    override fun getStringList(key: String, default: List<String>): List<String>
        = cachedList(key, default) { manager.getStringList(key, default) }

    override fun <T : Enum<T>> getEnum(key: String, default: T, filter: Predicate<T>): T {
        return cachedEnum(key, default, filter) { enum ->
            default::class.values().firstOrNull { it.name.equals(enum, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                warnInvalidEntry(key, enum, default)
                default
            }
        }
    }

    override fun <T : Enum<T>> getEnumSet(key: String, default: Set<T>, clazz: KClass<T>, filter: Predicate<T>): Set<T> {
        return filteredCachedSet(key, default, filter) {
            // parse the config entry, warning about possibly invalid values
            manager.getStringSet(key).mapNotNullTo(HashSet()) { item ->
                clazz.values().firstOrNull { it.name.equals(item, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                    warnInvalidEntry(key, item)
                    null
                }
            }.let { if(it.isNotEmpty()) EnumSet.copyOf(it) else emptySet() }
        }
    }

    override fun <T : Enum<T>> getEnumList(key: String, default: List<T>, clazz: KClass<T>, filter: Predicate<T>): List<T> {
        return filteredCachedList(key, default, filter) {
            // parse the config entry, warning about possibly invalid values
            manager.getStringList(key).mapNotNull { item ->
                clazz.values().firstOrNull { it.name.equals(item, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                    warnInvalidEntry(key, item)
                    null
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Enum<T>> cachedEnum(key: String, default: T, filter: Predicate<T>, transformer: (enum: String) -> T?): T {
        return cache.getOrPut(key) {
            manager.getString(key)?.let { enum ->
                transformer(enum)?.takeIf { filter.test(it) } ?: run {
                    warnInvalidEntry(key, enum, default)
                    default
                }
            } ?: default
        }.let { runCatching { it as T }.getOrElse { cacheException(key, it, default) } }
    }

    protected fun <T : Enum<T>> cachedEnum(key: String, default: T, transformer: (enum: String) -> T?): T
        = cachedEnum(key, default, { true }, transformer)

    @Suppress("UNCHECKED_CAST")
    protected fun <T: Any> cachedStringBased(key: String, default: T, transformer: (String) -> T?): T {
        return cache.getOrPut(key) {
            manager.getString(key)?.let {
                transformer(it) ?: run {
                    warnInvalidEntry(key, it, default)
                    default
                }
            }
        }.let { runCatching { it as T }.getOrElse { cacheException(key, it, default) } }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: Any> cachedAny(key: String, default: T, supplier: () -> T?): T {
        return cache.getOrPut(key) {
            supplier() ?: run {
                warnInvalidEntry(key, manager.getString(key), default)
                return@getOrPut default
            }
        }.let { runCatching { it as T }.getOrElse { cacheException(key, cache[key], default) } }
    }

    // Get function for Sets

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> filteredCachedSet(key: String, default: Set<T>, filter: Predicate<T>, transformer: (entries: List<String>) -> Collection<T>): Set<T> {
        return cache.getOrPut(key) {
            // if file doesn't contains specified key, return and cache default instead
            if(!manager.contains(key)) return@getOrPut default
            // map the set using the transformer function
            manager.getStringList(key).let(transformer).filterTo(mutableSetOf()) { filter.test(it) }
        }.let { it as? Set<T> ?: (it as? Iterable<T>)
            ?.filterTo(mutableSetOf()) { item -> filter.test(item) }
            ?.also { list -> cache[key] = list }
            ?: cacheException(key, it, default) }
    }

    protected fun <T : Any> cachedSet(key: String, default: Set<T>, transformer: (entries: List<String>) -> Collection<T>): Set<T>
        = filteredCachedSet(key, default, { true }, transformer)

    // Get function for Lists

    @Suppress("UNCHECKED_CAST")
    protected fun <T> filteredCachedList(key: String, default: List<T>, filter: Predicate<T>, transformer: (entries: List<String>) -> Collection<T>): List<T> {
        return cache.getOrPut(key) {
            // if file doesn't contains specified key, return and cache default instead
            if(!manager.contains(key)) return@getOrPut default
            // map the list using the transformer function
            manager.getStringList(key).let(transformer).filter { filter.test(it) }
        }.let { it as? List<T> ?: (it as? Iterable<T>)
            ?.filter { item -> filter.test(item) }
            ?.also { list -> cache[key] = list } ?: cacheException(key, it, default) }
    }

    protected fun <T> cachedList(key: String, default: List<T>, transformer: (entries: List<String>) -> Collection<T>): List<T>
        = filteredCachedList(key, default, { true }, transformer)

    // Lazy get function for lists

    @Suppress("UNCHECKED_CAST")
    protected fun <T> filteredCachedList(key: String, default: Supplier<List<T>>, filter: Predicate<T>, transformer: (entries: List<String>) -> Collection<T>): List<T> {
        return cache.getOrPut(key) {
            // if file doesn't contains specified key, return and cache default instead
            if(!manager.contains(key)) return@getOrPut default.get()
            // map the list using the transformer function
            manager.getStringList(key).let(transformer).filter { filter.test(it) }
        }.let { it as? List<T> ?: (it as? Iterable<T>)
            ?.filter { item -> filter.test(item) }
            ?.also { list -> cache[key] = list } ?: cacheException(key, it, default) }
    }

    protected fun <T> cachedList(key: String, default: Supplier<List<T>>, transformer: (entries: List<String>) -> Collection<T>): List<T>
        = filteredCachedList(key, default, { true }, transformer)

    // returns a pair of ints containing the <Min, Max> value of that property
    // the range can be just one number, or a range made like "1 - 5"
    @Suppress("UNCHECKED_CAST")
    override fun getIntRange(key: String, default: Int, minValue: Int, maxValue: Int): Pair<Int, Int> {
        return cache.getOrPut(key) {
            val values = manager.getString(key) ?: ""

            // if there's no amount field, return pair with default values
            if(values.isBlank()) return@getOrPut Pair(default, default)

            // typed amount is not an integer
            return RangeParser.parseInt(values, default, minValue, maxValue) { warnWrongType(key, values, default) }
        }.let { runCatching { it as Pair<Int, Int> }.getOrElse { cacheException(key, it, default) } }
    }

    // returns a pair of doubles containing the <Min, Max> value of that property
    @Suppress("UNCHECKED_CAST")
    override fun getDoubleRange(key: String, default: Double, minValue: Double, maxValue: Double): Pair<Double, Double> {
        return cache.getOrPut(key) {
            val values = manager.getString(key) ?: ""

            // if there's no key, return pair with default values
            if(values.isBlank()) return@getOrPut Pair(default, default)

            // typed amount is not an integer
            return RangeParser.parseDouble(values, default, minValue, maxValue) { warnWrongType(key, values, default) }
        }.let { runCatching { it as Pair<Double, Double> }.getOrElse { cacheException(key, it, default) } }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIntSequence(key: String, default: Set<Int>, minValue: Int, maxValue: Int): Set<Int> {
        return cache.getOrPut(key) {
            val values = manager.getString(key) ?: ""

            // if there's no key, return default set
            if(values.isBlank()) return@getOrPut default

            return RangeParser.parseIntOrNull(values) { warnWrongType(key, values, default.joinToString()) }
                ?.let { IntRange(it.first, it.second).toHashSet() }
                // typed amount is not an integer
                ?: default
        }.let { runCatching { it as Set<Int> }.getOrElse { cacheException(key, it, default) } }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIntSequenceList(key: String, default: Set<Int>, minValue: Int, maxValue: Int): Set<Int> {
        return cache.getOrPut(key) {
            // if there's no key, return default values
            if(!manager.contains(key)) return@getOrPut default

            val values = manager.getStringList(key)

            return@getOrPut values.mapNotNull { range ->
                RangeParser.parseIntOrNull(range, minValue, maxValue)
                    ?.let { IntRange(it.first, it.second).toHashSet() }
            }.flatten().filterTo(mutableSetOf()) { it in minValue..maxValue }
        }.let { runCatching { it as Set<Int> }.getOrElse { cacheException(key, it, default) } }
    }

    /**
     * Used to reduce boilerplace code but still warn end users about their
     * config mistakes, this method warns about type mismatch in single value
     * configuration fields.
     *
     * @param key String config key of the entry
     * @param default T the default value of the entry
     * @param got Any? what actually came when method [YamlManager#get()][YamlManager.get] was invoked
     */
    protected fun <T : Any> warnWrongType(key: String, got: Any?, default: T) {
        logger.warning(CONFIG_TYPE_MISMATCH_WITH_DEFAULT.format(manager.file, key, default::class.simpleName, got?.let { it::class.simpleName } ?: "null", manager.file, default))
    }

    protected fun warnInvalidEntry(key: String, invalidEntry: String?) {
        logger.warning(INVALID_ENUM_ENTRY.format(file, key, invalidEntry ?: "null", file.substring(0, file.lastIndex - 3)))
    }

    protected fun warnInvalidEntry(key: String, invalidEntry: String?, default: Any) {
        logger.warning(INVALID_ENUM_ENTRY_WITH_DEFAULT.format(file, key, invalidEntry ?: "null", file.substring(0, file.lastIndex - 3), default))
    }

    protected fun cacheException(key: String, previous: Any?, default: Any): Nothing {
        throw DifferentCachedTypeException(DIFFERENT_CACHED_TYPE.format(key, previous?.let { it::class.simpleName } ?: "null", default::class.simpleName, file.substring(0, file.lastIndex - 3)))
    }

    protected companion object {
        const val CONFIG_TYPE_MISMATCH_WITH_DEFAULT = "[%s] On key '%s', expected value of type '%s' but got '%s' instead, please fix your '%s' configurations and reload the configs, temporarily defaulting to %s."
        const val INVALID_ENUM_ENTRY = "[%s] On key '%s', value passed '%s' is invalid, please fix this entry in the %s and reload the configs."
        const val INVALID_ENUM_ENTRY_WITH_DEFAULT = "[%s] Error while trying to get key '%s', value passed '%s' is invalid, please fix this entry in the %s and reload the configs, temporarily defaulting to %s."
        const val DIFFERENT_CACHED_TYPE = "Tried to override previous cached value for key '%s' type '%s' with '%s' in file '%s', please check all your get methods for that key and make sure they're all requiring the same type. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue."
    }

}
