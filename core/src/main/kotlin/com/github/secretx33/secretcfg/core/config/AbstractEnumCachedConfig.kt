package com.github.secretx33.secretcfg.core.config

import com.github.secretx33.secretcfg.core.exception.InvalidDefaultParameterException
import java.io.File
import java.util.function.Predicate
import java.util.logging.Logger
import kotlin.reflect.KClass

abstract class AbstractEnumCachedConfig<U> (
    plugin: Any,
    dataFolder: File,
    path: String,
    logger: Logger,
    copyDefault: Boolean,
) : AbstractCachedConfig(plugin, dataFolder, path, logger, copyDefault) where U : ConfigEnum, U : Enum<U> {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: U, default: Any = key.default): T = get(key.path, default) as T

    fun getInt(
        key: U,
        default: Int = key.default as? Int ?: wrongDefault(key, Int::class),
        minValue: Int = 0,
        maxValue: Int = Int.MAX_VALUE
    ) = getInt(key.path, default, minValue, maxValue)

    fun getDouble(
        key: U,
        default: Double = key.default as? Double ?: wrongDefault(key, Double::class),
        minValue: Double = 0.0,
        maxValue: Double = Double.MAX_VALUE
    ) = getDouble(key.path, default, minValue, maxValue)

    @Suppress("UNCHECKED_CAST")
    fun getStringList(
        key: U,
        default: List<String> = key.default as? List<String> ?: wrongDefault(key),
    ) = getStringList(key.path, default)

    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> getEnum(
        key: U,
        default: T = key.default as? T ?: wrongDefault(key),
        filter: Predicate<T> = Predicate { true }
    ): T = getEnum(key.path, default, filter)

    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> getEnumSet(
        key: U,
        clazz: KClass<T>,
        default: Set<T> = key.default as? Set<T> ?: wrongDefault(key, clazz),
        filter: Predicate<T> = Predicate { true }
    ): Set<T> = getEnumSet(key.path, default, clazz, filter)

    fun getIntRange(
        key: U,
        default: Int = key.default as? Int ?: wrongDefault(key, Int::class),
        minValue: Int = 0,
        maxValue: Int = Int.MAX_VALUE
    ): Pair<Int, Int> = getIntRange(key.path, default, minValue, maxValue)

    fun getDoubleRange(
        key: U,
        default: Double = key.default as? Double ?: wrongDefault(key, Double::class),
        minValue: Double = 0.0,
        maxValue: Double = Double.MAX_VALUE
    ): Pair<Double, Double> = getDoubleRange(key.path, default, minValue, maxValue)

    fun set(key: U, value: Any) {
        set(key.path, value)
    }

    private fun wrongDefault(key: U): Nothing {
        throw InvalidDefaultParameterException(WRONG_DEFAULT_PARAMETER_TYPE.format(key.name, key.default::class.simpleName, manager.fileName))
    }

    private fun wrongDefault(key: U, expectedType: KClass<*>): Nothing {
        throw InvalidDefaultParameterException(WRONG_DEFAULT_PARAMETER_TYPE_KNOWN_TYPE.format(key.name, expectedType::class.simpleName, key.default::class.simpleName, manager.fileName))
    }

    private companion object {
        const val WRONG_DEFAULT_PARAMETER_TYPE = "Default parameter provided for config key '%s' is not from the expected type T, but instead its type is '%s', please fix your default parameter of this key from your EnumConfigClass of file '%s'. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue."
        const val WRONG_DEFAULT_PARAMETER_TYPE_KNOWN_TYPE = "Default parameter provided for config key '%s' is not from the expected type '%s', but instead its type is '%s', please fix your default parameter of this key from your EnumConfigClass of file '%s'. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue."
    }
}
