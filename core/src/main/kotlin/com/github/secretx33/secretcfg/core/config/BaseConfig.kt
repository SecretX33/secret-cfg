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

import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * Base interface to expose cached methods to the consumers
 * @since 1.0
 */
interface BaseConfig {

    /**
     * Name of the file, with extension.
     *
     * @since 1.0
     */
    val file: String

    /**
     * Name of the file, without extension.
     *
     * @since 1.0
     */
    val fileWithoutExtension: String

    /**
     * Relative path from the jar perspective.
     *
     * @since 1.0
     */
    val path: String

    /**
     * Reload the configs, forcing the values to be cached again
     * @since 1.0
     */
    fun reload()

    /**
     * Checks in the file if a certain key is present
     *
     * @param key String The key to be checked
     * @return Boolean Returns true if the key exists
     * @since 1.0
     */
    fun has(key: String): Boolean

    /**
     * Alias for method [has].
     *
     * @since 1.0
     * @see has
     */
    fun contains(key: String): Boolean

    /**
     * Sets a value in a key on the file, does not persist the changes, user must manually call [save] afterwards
     *
     * @param key String The key where the [value] should be saved at
     * @param value Any The value to be saved on the [key]
     * @since 1.0
     */
    fun set(key: String, value: Any)

    /**
     * Sets a Boolean in a key on the file, does not persist the changes, user must manually call [save] afterwards
     *
     * @param key String The key where the [value] should be saved at
     * @param value Boolean The boolean to be saved on the [key]
     * @since 1.0
     */
    fun setBoolean(key: String, value: Boolean)

    /**
     * Sets an Int in a key on the file, does not persist the changes, user must manually call [save] afterwards
     *
     * @param key String The key where the [value] should be saved at
     * @param value Int The int to be saved on the [key]
     * @since 1.0
     */
    fun setInt(key: String, value: Int)

    /**
     * Sets a value in a key on the file, does not persist the changes, user must manually call [save] afterwards
     *
     * @param key String The key where the [value] should be saved at
     * @param value Double The double to be saved on the [key]
     * @since 1.0
     */
    fun setDouble(key: String, value: Double)

    /**
     * Sets a String in a key on the file, does not persist the changes, user must manually call [save] afterwards
     *
     * @param key String The key where the [value] should be saved at
     * @param value String The string to be saved on the [key]
     * @since 1.0
     */
    fun setString(key: String, value: String)

    /**
     * Save the currently edited configurations to the file, this operation should not erase any comments of the file.
     *
     * @since 1.0
     */
    fun save()

    /**
     * Get the keys directly under root.
     *
     * @since 1.0
     */
    fun getKeys(): Set<String>

    /**
     * Get the keys directly under path.
     *
     * @since 1.0
     */
    fun getKeys(path: String): Set<String>

    /**
     * Retrieves a generic value from the config file, or default value if the key is missing
     *
     * @param key [String] Where the value is at
     * @param default [T] A default value in case the specified [key] is missing
     * @return [T] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun <T : Any> get(key: String, default: T): T

    /**
     * Retrieves a Int value from the config file, or the default value if the key is missing.
     *
     * @param key [String] Where the Float is at
     * @param default [Int] A default value in case the specified [key] is missing
     * @param minValue [Int] Minimum value that can be returned by this function
     * @param maxValue [Int] Maximum value that can be returned by this function
     * @return [Int] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun getInt(key: String, default: Int = 0, minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Int

    /**
    * Retrieves a Float value from the config file, or the default value if the key is missing.
    *
    * @param key [String] Where the Float is at
    * @param default [Float] A default value in case the specified [key] is missing
    * @param minValue [Float] Minimum value that can be returned by this function
    * @param maxValue [Float] Maximum value that can be returned by this function
    * @return [Float] The retrieved value, or [default] in case the key was missing
    * @since 1.0
    */
    fun getFloat(key: String, default: Float = 0f, minValue: Float = 0f, maxValue: Float = Float.MAX_VALUE): Float

    /**
     * Retrieves a Double value from the config file, or the default value if the key is missing.
     *
     * @param key [String] Where the Double is at
     * @param default [Double] A default value in case the specified [key] is missing
     * @param minValue [Double] Minimum value that can be returned by this function
     * @param maxValue [Double] Maximum value that can be returned by this function
     * @return [Double] The retrieved value, or [default] in case the key was missing
     * @since 1.0
     */
    fun getDouble(key: String, default: Double = 0.0, minValue: Double = 0.0, maxValue: Double = Double.MAX_VALUE): Double

    /**
     * Retrieves a String from the config file, or the default value if the key is missing.
     *
     * @param key [String] Where the String is at
     * @param default [String] A default value in case the specified [key] is missing
     * @return [String] The retrieved string, or [default] in case the key was missing
     * @since 1.0
     */
    fun getString(key: String, default: String): String

    /**
     * Retrieves a string list from the config file
     *
     * @param key [String] Where the string list is stored at
     * @param default [List<String>] A fallback in case the specified [key] is missing
     * @return [List<String>] A list containing the strings retrieved from the config file, or the [default] list in case the key was missing
     * @since 1.0
     */
    fun getStringList(key: String, default: List<String> = emptyList()): List<String>

    /**
     * Gets a single enum from a config key, optionally filtering the enum.
     *
     * @param key [String] Where the enum is stored at
     * @param default [T] A default value in case the entry is missing or invalid
     * @param filter [Predicate<T>] A predicate to filter the item retrieved from the config
     * @return [T] The enum retrieved from the config, in case it was present and was accepted by the [filter], or the [default] value
     * @since 1.0
     */
    fun <T : Enum<T>> getEnum(key: String, default: T, filter: Predicate<T> = Predicate { true }): T

    /**
     * Gets a Set of enums from the file, optionally filtering the entries with the given predicate. Keep in mind that the Set will only be cached after the filter is applied, so if you want to be able to retrieve the full set later, use [Iterable.filter] function on site instead.
     *
     * @param key [String] Where the enum set is stored at
     * @param default [Set<T>] A default set containing default values in case the
     * @param clazz [KClass<T>] The enum class you want to get the values from
     * @param filter [Predicate<T>] A predicate to filter certain values, be aware that the user will be warned if your predicate reject some of their items
     * @return [Set<T>] A Set containing the parsed Enums, or in case the entry was missing, the default Set instead.
     * @since 1.0
     */
    fun <T : Enum<T>> getEnumSet(key: String, default: Set<T> = emptySet(), clazz: KClass<T>, filter: Predicate<T> = Predicate { true }): Set<T>


    /**
     * Gets a List of enums from the file, optionally filtering the entries with the given predicate. Keep in mind that the List will only be cached after the filter is applied, so if you want to be able to retrieve the full set later, use [Iterable.filter] function on site instead.
     *
     * @param key [String] Where the enum list is stored at
     * @param default [List<T>] A default list containing default values in case the
     * @param clazz [KClass<T>] The enum class you want to get the values from
     * @param filter [Predicate<T>] A predicate to filter certain values, be aware that the user will be warned if your predicate reject some of their items
     * @return [List<T>] A List containing the parsed Enums, or in case the entry was missing, the default List instead.
     * @since 1.0
     */
    fun <T : Enum<T>> getEnumList(key: String, default: List<T> = emptyList(), clazz: KClass<T>, filter: Predicate<T> = Predicate { true }): List<T>

    /**
     * Gets a Pair of Ints containing the <Min, Max> value of that Int range. the range can be just one number, or a range made like "1 - 5"/
     *
     * @param key [String] Where the range is stored at
     * @param default [Int] A fallback value in case the value in the path specified is missing or incorrect
     * @param minValue [Int] Limits the minimum value in the pair to the specified value
     * @param maxValue [Int] Optionally, limits the maximum value in the pair to the specified value
     * @return [Pair<Int, Int>] A pair containing the <Min, Max> of the Int range in the key
     * @since 1.0
     */
    fun getIntRange(key: String, default: Int = 0, minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Pair<Int, Int>

    /**
     * Gets a Pair of Doubles containing the <Min, Max> value of that Double range. The range can be just one number, or a range made like "1 - 5".
     *
     * @param key [String] Where the range is stored at
     * @param default [Double] A fallback value in case the value in the path specified is missing or incorrect
     * @param minValue [Double] Limits the minimum value in the pair to the specified value
     * @param maxValue [Double] Optionally, limits the maximum value in the pair to the specified value
     * @return [Pair<Double, Double>] A pair containing the <Min, Max> of the Double range in the key
     * @since 1.0
     */
    fun getDoubleRange(key: String, default: Double = 0.0, minValue: Double = 0.0, maxValue: Double = Double.MAX_VALUE): Pair<Double, Double>

    /**
     * Get all integers within the range given by the user, e.g., "0 - 8" would produce a set equivalent to `setOf(0..8)`,
     * unsorted.
     *
     * @param key String Where the range is stored at
     * @param default Set<Int> A fallback value in case the range in the path specified is missing or incorrect
     * @param minValue Int Specify the minimum Int value that can be in the Set
     * @param maxValue Int Specify the maximum Int value that can be in the Set
     * @return Set<Int> A sorted set containing all integers within user specified range
     * @since 1.0
     */
    fun getIntSequence(key: String, default: Set<Int> = emptySet(), minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Set<Int>

    /**
     * Do the same as the function [getIntSequence], but read the ranges from a string list, instead of a single entry.
     * A range list containing "0-5" and "10-15" would produce a set equivalent to `setOf(0..5) + setOf(10..15)`, sorted
     * by their natural order.
     *
     * @param key String Where the range is stored at
     * @param default Set<Int> A fallback value in case the range in the path specified is missing or incorrect
     * @param minValue Int Specify the minimum Int value that can be in the Set
     * @param maxValue Int Specify the maximum Int value that can be in the Set
     * @return Set<Int> A sorted set containing all integers within user specified ranges
     * @since 1.0
     * @see getIntSequence
     */
    fun getIntSequenceList(key: String, default: Set<Int> = emptySet(), minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Set<Int>
}
