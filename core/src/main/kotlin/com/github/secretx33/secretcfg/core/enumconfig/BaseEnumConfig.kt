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

import com.github.secretx33.secretcfg.core.exception.InvalidDefaultParameterException
import com.github.secretx33.secretcfg.core.util.Predicates
import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * Base interface for enum configs to expose cached methods to the consumers
 * @since 1.0
 */
interface BaseEnumConfig<U> where U : ConfigEnum, U : Enum<U>  {

    /**
     * Enum class that hold the config keys
     *
     * @since 1.0
     */
    val configClass: KClass<U>

    val file: String

    private inline fun <reified T> U.safeDefault(): T = runCatching { default as T }.getOrElse { throw InvalidDefaultParameterException("Default parameter provided for config key '$name' is not from the expected type '${T::class::simpleName}', but instead its type is '${default::class.simpleName}', please fix your default parameter of this key from your ${configClass.simpleName} class of file '$file'. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue.") }

    /**
     * Checks in the file if a certain key is present
     *
     * @param key U The key to be checked
     * @return Boolean Returns true if the key exists
     * @since 1.0
     */
    fun has(key: U): Boolean

    /**
     * Alias for method [has].
     *
     * @since 1.0
     * @see has
     */
    fun contains(key: U): Boolean

    /**
     * Sets a value in a key on the file, does not persist the changes, user must manually call [save] afterwards
     *
     * @param key U The key where the [value] should be saved at
     * @param value Any The value to be saved on the [key]
     * @since 1.0
     */
    fun set(key: U, value: Any)

    /**
     * Retrieves a generic value from the config file, or use [configClass.default] value if the key is missing
     *
     * @param key [U] Where the value is at
     * @param default [T] A default value in case the specified [key] is missing
     * @return [T] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun <T : Any> get(key: U): T

    /**
     * Retrieves a generic value from the config file, or default value if the key is missing
     *
     * @param key [U] Where the value is at
     * @param default [T] A default value in case the specified [key] is missing
     * @return [T] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun <T : Any> get(key: U, default: T): T

    /**
     * Retrieves a Boolean value from the config file, or the default value if the key is missing.
     *
     * @param key [U] Where the Boolean is at
     * @param default [Boolean] A default value in case the specified [key] is missing
     * @return [Boolean] The retrieved Boolean, or [default] in case the key was missing
     * @since 1.0
     */
    fun getBoolean(key: U, default: Boolean = key.safeDefault()): Boolean

    /**
     * Retrieves an Int value from the config file, or the default value if the key is missing.
     *
     * @param key [U] Where the Int is at
     * @param default [Int] A default value in case the specified [key] is missing
     * @param minValue [Int] Minimum value that can be returned by this function
     * @param maxValue [Int] Maximum value that can be returned by this function
     * @return [Int] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun getInt(key: U, default: Int = key.safeDefault(), minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Int

    /**
     * Retrieves a Float value from the config file, or the default value if the key is missing.
     *
     * @param key [U] Where the Float is at
     * @param default [Float] A default value in case the specified [key] is missing
     * @param minValue [Float] Minimum value that can be returned by this function
     * @param maxValue [Float] Maximum value that can be returned by this function
     * @return [Float] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun getFloat(key: U, default: Float = key.safeDefault(), minValue: Float = 0f, maxValue: Float = Float.MAX_VALUE): Float

    /**
     * Retrieves a Double value from the config file, or the default value if the key is missing.
     *
     * @param key [U] Where the Double is at
     * @param default [Double] A default value in case the specified [key] is missing
     * @param minValue [Double] Minimum value that can be returned by this function
     * @param maxValue [Double] Maximum value that can be returned by this function
     * @return [Double] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun getDouble(key: U, default: Double = key.safeDefault(), minValue: Double = 0.0, maxValue: Double = Double.MAX_VALUE): Double

    /**
     * Retrieves a String from the config file, or the default value if the key is missing.
     *
     * @param key [U] Where the String is at
     * @param default [String] A default value in case the specified [key] is missing
     * @return [String] The retrieved string, or [default] in case the key was missing
     * @since 1.0
     */
    fun getString(key: U, default: String = key.safeDefault()): String

    /**
     * Retrieves a string list from the config file
     *
     * @param key [U] Where the string list is stored at
     * @param default [List<String>] A fallback in case the specified [key] is missing
     * @return [List<String>] A list containing the strings retrieved from the config file, or the [default] list in case the key was missing
     * @since 1.0
     */
    fun getStringList(key: U, default: List<String> = key.safeDefault()): List<String>

    /**
     * Gets a single enum from a config key, optionally filtering the enum.
     *
     * @param key [U] Where the enum is stored at
     * @param filter [Predicate<T>] A predicate to filter the item retrieved from the config
     * @return [T] The enum retrieved from the config, in case it was present and was accepted by the [filter], or the [default] value
     * @since 1.0
     */
    fun <T : Enum<T>> getEnum(key: U, filter: Predicate<T> = Predicates.accept()): T

    /**
     * Gets a single enum from a config key, optionally filtering the enum.
     *
     * @param key [U] Where the enum is stored at
     * @param default [T] A default value in case the entry is missing or invalid
     * @param filter [Predicate<T>] A predicate to filter the item retrieved from the config
     * @return [T] The enum retrieved from the config, in case it was present and was accepted by the [filter], or the [default] value
     * @since 1.0
     */
    fun <T : Enum<T>> getEnum(key: U, default: T, filter: Predicate<T> = Predicates.accept()): T

    /**
     * Gets a Set of enums from the file, optionally filtering the entries with the given predicate. Keep in mind that the Set will only be cached after the filter is applied, so if you want to be able to retrieve the full set later, use [Iterable.filter] function on site instead.
     *
     * @param key [U] Where the enum set is stored at
     * @param default [Set<T>] A default set containing default values in case the
     * @param clazz [KClass<T>] The enum class you want to get the values from
     * @param filter [Predicate<T>] A predicate to filter certain values, be aware that the user will be warned if your predicate reject some of their items
     * @return [Set<T>] A Set containing the parsed Enums, or in case the entry was missing, the default Set instead.
     * @since 1.0
     */
    fun <T : Enum<T>> getEnumSet(key: U, default: Set<T> = key.safeDefault(), clazz: KClass<T>, filter: Predicate<T> = Predicates.accept()): Set<T>

    /**
     * Gets a List of enums from the file, optionally filtering the entries with the given predicate. Keep in mind that the List will only be cached after the filter is applied, so if you want to be able to retrieve the full set later, use [Iterable.filter] function on site instead.
     *
     * @param key [U] Where the enum list is stored at
     * @param default [List<T>] A default list containing default values in case the
     * @param clazz [KClass<T>] The enum class you want to get the values from
     * @param filter [Predicate<T>] A predicate to filter certain values, be aware that the user will be warned if your predicate reject some of their items
     * @return [List<T>] A List containing the parsed Enums, or in case the entry was missing, the default List instead.
     * @since 1.0
     */
    fun <T : Enum<T>> getEnumList(key: U, default: List<T> = key.safeDefault(), clazz: KClass<T>, filter: Predicate<T> = Predicates.accept()): List<T>

    /**
     * Gets a Pair of Ints containing the <Min, Max> value of that Int range. the range can be just one number, or a range made like "1 - 5"/
     *
     * @param key [U] Where the range is stored at
     * @param default [Int] A fallback value in case the value in the path specified is missing or incorrect
     * @param minValue [Int] Limits the minimum value in the pair to the specified value
     * @param maxValue [Int] Optionally, limits the maximum value in the pair to the specified value
     * @return [Pair<Int, Int>] A pair containing the <Min, Max> of the Int range in the key
     * @since 1.0
     */
    fun getIntRange(key: U, default: Int = 0, minValue: Int = key.safeDefault(), maxValue: Int = Int.MAX_VALUE): Pair<Int, Int>

    /**
     * Gets a Pair of Doubles containing the <Min, Max> value of that Double range. The range can be just one number, or a range made like "1 - 5".
     *
     * @param key [U] Where the range is stored at
     * @param default [Double] A fallback value in case the value in the path specified is missing or incorrect
     * @param minValue [Double] Limits the minimum value in the pair to the specified value
     * @param maxValue [Double] Optionally, limits the maximum value in the pair to the specified value
     * @return [Pair<Double, Double>] A pair containing the <Min, Max> of the Double range in the key
     * @since 1.0
     */
    fun getDoubleRange(key: U, default: Double = 0.0, minValue: Double = key.safeDefault(), maxValue: Double = Double.MAX_VALUE): Pair<Double, Double>

    /**
     * Get all integers within the range given by the user, e.g., "0 - 8" would produce a set equivalent to `setOf(0..8)`,
     * sorted by their natural order.
     *
     * @param key U Where the range is stored at
     * @param default Set<Int> A fallback value in case the range in the path specified is missing or incorrect
     * @param minValue Int Specify the minimum Int value that can be in the Set
     * @param maxValue Int Specify the maximum Int value that can be in the Set
     * @return Set<Int> A sorted set containing all integers within user specified range
     * @since 1.0
     */
    fun getIntSequence(key: U, default: Set<Int> = key.safeDefault(), minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Set<Int>

    /**
     * Do the same as the function [getIntSequence], but read the ranges from a string list, instead of a single entry.
     * A range list containing "0-5" and "10-15" would produce a set equivalent to `setOf(0..5) + setOf(10..15)`, sorted
     * by their natural order.
     *
     * @param key U Where the range is stored at
     * @param default Set<Int> A fallback value in case the range in the path specified is missing or incorrect
     * @param minValue Int Specify the minimum Int value that can be in the Set
     * @param maxValue Int Specify the maximum Int value that can be in the Set
     * @return Set<Int> A sorted set containing all integers within user specified ranges
     * @since 1.0
     * @see getIntSequence
     */
    fun getIntSequenceList(key: U, default: Set<Int> = key.safeDefault(), minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Set<Int>
}
