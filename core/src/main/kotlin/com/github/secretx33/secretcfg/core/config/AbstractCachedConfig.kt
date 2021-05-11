package com.github.secretx33.secretcfg.core.config

import com.github.secretx33.secretcfg.core.manager.YamlManager
import com.github.secretx33.secretcfg.core.exception.DifferentCachedTypeException
import com.github.secretx33.secretcfg.core.utils.values
import io.leangen.geantyref.TypeToken
import java.io.File
import java.lang.ClassCastException
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

open class AbstractCachedConfig (
    plugin: Any,
    dataFolder: File,
    path: String,
    private val logger: Logger,
    copyDefault: Boolean,
) : BaseCachedConfig {

    protected val manager = YamlManager(plugin, dataFolder, path, logger, copyDefault)
    private val cache = ConcurrentHashMap<String, Any>()

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

    override fun save() = manager.save()

    override val fileName: String
        get() = manager.fileName

    override val path: String
        get() = manager.relativePath

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
        = cachedAny(key, default) { manager.getString(key) }

    override fun getStringList(key: String, default: List<String>): List<String>
        = cachedList(key, default) { manager.getStringList(key, default) }

    override fun <T : Enum<T>> getEnum(key: String, default: T, filter: Predicate<T>): T {
        return cachedEnum(key, default, filter) { enum ->
            default::class.values().firstOrNull { it.name.equals(enum, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                warnInvalidEnumEntry(key, enum, default)
                default
            }
        }
    }

    override fun <T : Enum<T>> getEnumSet(key: String, default: Set<T>, clazz: KClass<T>, filter: Predicate<T>): Set<T> {
        return filteredCachedSet(key, default, filter) {
            // parse the config entry, warning about possibly invalid values
            manager.getStringSet(key).mapNotNullTo(mutableSetOf()) { item ->
                clazz.values().firstOrNull { it.name.equals(item, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                    warnInvalidEnumEntry(key, item)
                    null
                }
            }
        }
    }

    override fun <T : Enum<T>> getEnumList(key: String, default: List<T>, clazz: KClass<T>, filter: Predicate<T>): List<T> {
        return filteredCachedList(key, default, filter) {
            // parse the config entry, warning about possibly invalid values
            manager.getStringList(key).mapNotNull { item ->
                clazz.values().firstOrNull { it.name.equals(item, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                    warnInvalidEnumEntry(key, item)
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
                    warnInvalidEnumEntry(key, enum, default)
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
                    warnInvalidEnumEntry(key, it, default)
                    default
                }
            }
        }.let { runCatching { it as T }.getOrElse { cacheException(key, it, default) } }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: Any> cachedAny(key: String, default: T, supplier: () -> T?): T {
        return cache.getOrPut(key) {
            supplier() ?: run {
                warnInvalidEnumEntry(key, manager.getString(key), default)
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

            // value is only one value
            SIGNED_INT.matchEntire(values)?.groupValues?.get(1)
                ?.let { min(maxValue, max(minValue, it.toInt())) }
                ?.let { return@getOrPut Pair(it, it) }

            // value is a range of values
            SIGNED_INT_RANGE.matchEntire(values)?.groupValues
                ?.subList(1, 3)
                ?.map { min(maxValue, max(minValue, it.toInt())) }
                ?.let { return@getOrPut Pair(it[0], max(it[0], it[1])) }

            // typed amount is not an integer
            warnWrongType(key, values, default)
            return@getOrPut Pair(default, default)
        }.let { runCatching { it as Pair<Int, Int> }.getOrElse { cacheException(key, it, default) } }
    }

    // returns a pair of doubles containing the <Min, Max> value of that property
    @Suppress("UNCHECKED_CAST")
    override fun getDoubleRange(key: String, default: Double, minValue: Double, maxValue: Double): Pair<Double, Double> {
        return cache.getOrPut(key) {
            val values = manager.getString(key) ?: ""

            // if there's no key, return pair with default values
            if(values.isBlank()) return@getOrPut Pair(default, default)

            // value is only one value
            SIGNED_DOUBLE.matchEntire(values)?.groupValues?.get(1)
                ?.let { min(maxValue, max(minValue, it.toDouble())) }
                ?.let { return@getOrPut Pair(it, it) }

            // value is a range of values
            SIGNED_DOUBLE_RANGE.matchEntire(values)?.groupValues
                ?.subList(1, 3)
                ?.map { min(maxValue, max(minValue, it.toDouble())) }
                ?.let { return@getOrPut Pair(it[0], max(it[0], it[1])) }

            // typed amount is not a double
            warnWrongType(key, values, default)
            return@getOrPut Pair(default, default)
        }.let { runCatching { it as Pair<Double, Double> }.getOrElse { cacheException(key, it, default) } }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIntSequence(key: String, default: Set<Int>, minValue: Int, maxValue: Int): Set<Int> {
        return cache.getOrPut(key) {
            val values = manager.getString(key) ?: ""

            // if there's no key, return default set
            if(values.isBlank()) return@getOrPut default

            // value is only one value
            SIGNED_INT.matchEntire(values)?.groupValues?.get(1)
                ?.let { min(maxValue, max(minValue, it.toInt())) }
                ?.let { return@getOrPut setOf(it) }

            // value is a range of values
            SIGNED_INT_RANGE.matchEntire(values)?.groupValues
                ?.subList(1, 3)
                ?.map { min(maxValue, max(minValue, it.toInt())) }
                ?.let { return@getOrPut IntRange(it[0], it[1]).toSet() }

            // typed amount is not an integer
            warnWrongType(key, values, default.joinToString())
            return@getOrPut Pair(default, default)
        }.let { runCatching { it as Set<Int> }.getOrElse { cacheException(key, it, default) } }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIntSequenceList(key: String, default: Set<Int>, minValue: Int, maxValue: Int): Set<Int> {
        return cache.getOrPut(key) {
            // if there's no key, return default values
            if(!manager.contains(key)) return@getOrPut default

            val values = manager.getStringList(key)

            return@getOrPut values.mapNotNull { range ->
                SIGNED_INT.matchEntire(range)?.groupValues?.get(1)
                    ?.let { max(0, it.toInt()) }
                    ?.let { return@mapNotNull setOf(it) }

                // value is a range of values
                SIGNED_INT_RANGE.matchEntire(range)?.groupValues
                    ?.subList(1, 3)
                    ?.map { max(0, it.toInt()) }
                    ?.let { return@mapNotNull IntRange(it[0], it[1]).toSet() }
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
        logger.warning(CONFIG_TYPE_MISMATCH_WITH_DEFAULT.format(manager.fileName, key, default::class.simpleName, got?.let { it::class.simpleName } ?: "null", manager.fileName, default))
    }

    protected fun warnInvalidEnumEntry(key: String, invalidEntry: String?) {
        logger.warning(INVALID_ENUM_ENTRY.format(manager.fileName, key, invalidEntry ?: "null", manager.fileName))
    }

    protected fun warnInvalidEnumEntry(key: String, invalidEntry: String?, default: Any) {
        logger.warning(INVALID_ENUM_ENTRY_WITH_DEFAULT.format(manager.fileName, key, invalidEntry ?: "null", manager.fileName, default))
    }

    protected fun cacheException(key: String, previous: Any?, default: Any): Nothing {
        throw DifferentCachedTypeException(DIFFERENT_CACHED_TYPE.format(key, previous?.let { it::class.simpleName } ?: "null", default::class.simpleName, manager.fileName))
    }

    protected companion object {
        const val CONFIG_TYPE_MISMATCH_WITH_DEFAULT = "On file %s key '%s', expected value of type '%s' but got '%s' instead, please fix your '%s' configurations and reload the configs, temporarily defaulting to %s."
        const val INVALID_ENUM_ENTRY = "Error while trying to get file %s entry key '$%s', value passed '%s' is invalid, please fix this entry in the %s and reload the configs."
        const val INVALID_ENUM_ENTRY_WITH_DEFAULT = "Error while trying to get file %s key '%s', value passed '%s' is invalid, please fix this entry in the %s and reload the configs, temporarily defaulting to %s."
        const val DIFFERENT_CACHED_TYPE = "Tried to override previous cached value for key '%s' type '%s' with '%s' in file '%s', please check all your get methods for that key and make sure they're all requiring the same type. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue."

        // regex matching a range of two integers
        val SIGNED_INT = """^\s*(-?\d{1,11})\s*$""".toRegex()                                           // "-5"           ->  -5
        val SIGNED_INT_RANGE = """^\s*(-?\d{1,11}?)\s*-\s*(-?\d{1,11})\s*$""".toRegex()                 // "-5 - -1"      ->  -5 until -1

        // regex matching a range of two doubles
        val SIGNED_DOUBLE = """^\s*(-?\d+?(?:\.\d+?)?)\s*$""".toRegex()                                 // "-5.0"         ->  -5.0
        val SIGNED_DOUBLE_RANGE = """^\s*(-?\d+?(?:\.\d+?)?)\s*-\s*(-?\d+?(?:\.\d+)?)\s*$""".toRegex()  // "-5.0 - -1.0"  ->  -5.0 until -1.0
    }

}
