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
package com.github.secretx33.secretcfg.core.enumconfig

import com.github.secretx33.secretcfg.core.config.AbstractConfig
import com.github.secretx33.secretcfg.core.exception.InvalidDefaultParameterException
import java.io.File
import java.util.function.Predicate
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class AbstractEnumConfig<U> (
    plugin: Any,
    dataFolder: File,
    override val configClass: KClass<U>,
    path: String,
    logger: Logger,
    copyDefault: Boolean,
    filePresentInJar: Boolean,
) : AbstractConfig(plugin, dataFolder, path, logger, copyDefault, filePresentInJar), BaseEnumConfig<U> where U : ConfigEnum, U : Enum<U>  {

    override fun has(key: U): Boolean = has(key.name)

    override fun contains(key: U): Boolean = contains(key.name)

    override fun set(key: U, value: Any) = set(key.name, value)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: U): T = get(key.name, runCatching { key.safeDefaultGeneric(key.default::class as KClass<T>) }.getOrElse { wrongDefault(key) })

    override fun <T : Any> get(key: U, default: T): T = get(key, default)

    override fun getBoolean(key: U, default: Boolean): Boolean
        = getBoolean(key.name, default)

    override fun getInt(key: U, default: Int, minValue: Int, maxValue: Int): Int
        = getInt(key.name, default, minValue, maxValue)

    override fun getDouble(key: U, default: Double, minValue: Double, maxValue: Double): Double
        = getDouble(key.name, default, minValue, maxValue)

    override fun getFloat(key: U, default: Float, minValue: Float, maxValue: Float): Float
        = getFloat(key.name, default, minValue, maxValue)

    override fun getString(key: U, default: String): String = getString(key.name, default)

    override fun getStringList(key: U, default: List<String>): List<String> = getStringList(key.name, default)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Enum<T>> getEnum(key: U, filter: Predicate<T>): T
        = getEnum(key.name, runCatching { key.safeDefaultGeneric(key.default::class as KClass<T>) }.getOrElse { wrongDefault(key) }, filter)

    override fun <T : Enum<T>> getEnum(key: U, default: T, filter: Predicate<T>): T
        = getEnum(key.name, default, filter)

    override fun <T : Enum<T>> getEnumSet(key: U, default: Set<T>, clazz: KClass<T>, filter: Predicate<T>): Set<T>
        = getEnumSet(key.name, default, clazz, filter)

    override fun <T : Enum<T>> getEnumList(key: U, default: List<T>, clazz: KClass<T>, filter: Predicate<T>): List<T>
        = getEnumList(key.name, default, clazz, filter)

    override fun getIntRange(key: U, default: Int, minValue: Int, maxValue: Int): Pair<Int, Int>
        = getIntRange(key.name, default, minValue, maxValue)

    override fun getDoubleRange(key: U, default: Double, minValue: Double, maxValue: Double): Pair<Double, Double>
        = getDoubleRange(key.name, default, minValue, maxValue)

    override fun getIntSequence(key: U, default: Set<Int>, minValue: Int, maxValue: Int): Set<Int>
        = getIntSequence(key.name, default, minValue, maxValue)

    override fun getIntSequenceList(key: U, default: Set<Int>, minValue: Int, maxValue: Int): Set<Int>
        = getIntSequenceList(key.name, default, minValue, maxValue)

    private fun <T : Any> U.safeDefaultGeneric(clazz: KClass<T>): T = runCatching { clazz.cast(default) }.getOrElse { wrongDefault(this)}

    private fun wrongDefault(key: U): Nothing {
        throw InvalidDefaultParameterException(WRONG_DEFAULT_PARAMETER_TYPE.format(key.name, key.default::class.simpleName, configClass::class.simpleName, manager.fileName))
    }

    private fun wrongDefault(key: U, expectedType: KClass<*>): Nothing {
        throw InvalidDefaultParameterException(WRONG_DEFAULT_PARAMETER_TYPE_KNOWN_TYPE.format(key.name, expectedType::class.simpleName, key.default::class.simpleName, configClass::class.simpleName, manager.fileName))
    }

    private companion object {
        const val WRONG_DEFAULT_PARAMETER_TYPE = "Default parameter provided for config key '%s' is not from the expected type T, but instead its type is '%s', please fix your default parameter of this key from your %s class of file '%s'. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue."
        const val WRONG_DEFAULT_PARAMETER_TYPE_KNOWN_TYPE = "Default parameter provided for config key '%s' is not from the expected type '%s', but instead its type is '%s', please fix your default parameter of this key from your %s class of file '%s'. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue."
    }
}
