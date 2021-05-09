package com.github.secretx33.secretcfg.core

import com.github.secretx33.secretcfg.utils.values
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

abstract class BaseCachedConfig (
    plugin: Any,
    dataFolder: File,
    path: String,
    private val logger: Logger,
    copyDefault: Boolean,
) {

    protected val manager = YamlManager(plugin, dataFolder, path, logger, copyDefault)
    protected val cache = ConcurrentHashMap<String, Any>()

    fun reload() {
        cache.clear()
        manager.reload()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String, default: T): T {
        return cache.getOrPut(key) {
            manager.get(key) as? T ?: run {
                warnWrongType(key, manager.get(key), default)
                default
            }
        } as T
    }

    fun getInt(key: String, default: Int, minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Int {
        return cache.getOrPut(key) {
            (manager.get(key) as? Int)?.let { int -> max(minValue, min(maxValue, int)) } ?: run {
                warnWrongType(key, manager.get(key), default)
                default
            }
        } as Int
    }

    fun getFloat(key: String, default: Float, minValue: Float = 0f, maxValue: Float = Float.MAX_VALUE): Float {
        return cache.getOrPut(key) {
            (manager.get(key, default) as? Float)?.let { float -> max(minValue, min(maxValue, float)) } ?: run {
                warnWrongType(key, manager.get(key), default)
                default
            }
        } as Float
    }

    fun getDouble(key: String, default: Double, minValue: Double = 0.0, maxValue: Double = Double.MAX_VALUE): Double {
        return cache.getOrPut(key) {
            (manager.get(key, default) as? Double)?.let { double -> max(minValue, min(maxValue, double)) } ?: run {
                warnWrongType(key, manager.get(key), default)
                default
            }
        } as Double
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringList(key: String, default: List<String> = emptyList()): List<String> {
        return cache.getOrPut(key) {
            manager.getStringList(key, default)
        } as List<String>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> getEnum(key: String, default: T, filter: Predicate<T> = Predicate { true }): T {
        return cache.getOrPut(key) {
            manager.getString(key)?.let { enum ->
                default::class.values().firstOrNull { it.name.equals(enum, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                    warnInvalidEnumEntry(key, enum, default)
                    default
                }
            } ?: default
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> getEnumSet(key: String, default: Set<T>, clazz: KClass<T>, filter: Predicate<T> = Predicate { true }): Set<T> {
        return cache.getOrPut(key) {
            // if file doesn't contains specified key, cache and return default instead
            if(!manager.contains(key)) return@getOrPut default

            // parse the config entry, warning about possibly invalid values
            manager.getStringSet(key).mapNotNullTo(mutableSetOf()) { item ->
                clazz.values().firstOrNull { it.name.equals(item, ignoreCase = true) }?.takeIf { filter.test(it) } ?: run {
                    warnInvalidEnumEntry(key, item)
                    null
                }
            }
        } as Set<T>
    }

    // returns a pair of ints containing the <Min, Max> value of that property
    // the range can be just one number, or a range made like "1 - 5"
    @Suppress("UNCHECKED_CAST")
    fun getIntRange(key: String, default: Int, minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Pair<Int, Int> {
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
        } as Pair<Int, Int>
    }

    // returns a pair of doubles containing the <Min, Max> value of that property
    @Suppress("UNCHECKED_CAST")
    fun getDoubleRange(key: String, default: Double, minValue: Double = 0.0, maxValue: Double = Double.MAX_VALUE): Pair<Double, Double> {
        return cache.getOrPut(key) {
            val values = manager.getString(key) ?: ""

            // if there's no amount field, return pair with default values
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
        } as Pair<Double, Double>
    }

    fun has(path: String): Boolean = manager.contains(path)

    fun set(key: String, value: Any) {
        cache[key] = value
        manager.set(key, value)
    }

    fun save() = manager.save()

    /**
     * Used to reduce boilerplace code but still warn end users about their
     * config mistakes, this method warns about type mismatch in single value
     * configuration fields.
     *
     * @param key String config key of the entry
     * @param default T the default value of the entry
     * @param got Any? what actually came when method [YamlManager.get] was invoked
     */
    protected fun <T : Any> warnWrongType(key: String, got: Any?, default: T) {
        logger.severe(CONFIG_TYPE_MISMATCH_WITH_DEFAULT.format(manager.fileName, key, default::class.simpleName, got?.let { it::class.simpleName } ?: "null", manager.fileName, default))
    }

    protected fun warnInvalidEnumEntry(key: String, invalidEntry: String) {
        logger.severe(INVALID_ENUM_ENTRY.format(manager.fileName, key, invalidEntry, manager.fileName))
    }

    protected fun warnInvalidEnumEntry(key: String, invalidEntry: String, default: Any) {
        logger.severe(INVALID_ENUM_ENTRY_WITH_DEFAULT.format(manager.fileName, key, invalidEntry, manager.fileName, default))
    }

    protected companion object {
        const val CONFIG_TYPE_MISMATCH_WITH_DEFAULT = "On file %s key '%s', expected value of type '%s' but got '%s' instead, please fix your '%s' configurations and reload the configs, temporarily defaulting to %s."
        const val INVALID_ENUM_ENTRY = "Error while trying to get file %s entry key '$%s', value passed '%s' is invalid, please fix this entry in the %s and reload the configs."
        const val INVALID_ENUM_ENTRY_WITH_DEFAULT = "Error while trying to get file %s key '%s', value passed '%s' is invalid, please fix this entry in the %s and reload the configs, temporarily defaulting to %s."

        // regex matching a range of two integers
        val SIGNED_INT = """^\s*(-?\d{1,11})\s*$""".toRegex()                                           // "-5"           ->  -5
        val SIGNED_INT_RANGE = """^\s*(-?\d{1,11}?)\s*-\s*(-?\d{1,11})\s*$""".toRegex()                 // "-5 - -1"      ->  -5 until -1

        // regex matching a range of two doubles
        val SIGNED_DOUBLE = """^\s*(-?\d+?(?:\.\d+?)?)\s*$""".toRegex()                                 // "-5.0"         ->  -5.0
        val SIGNED_DOUBLE_RANGE = """^\s*(-?\d+?(?:\.\d+?)?)\s*-\s*(-?\d+?(?:\.\d+)?)\s*$""".toRegex()  // "-5.0 - -1.0"  ->  -5.0 until -1.0
    }

}
