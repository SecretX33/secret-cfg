package com.github.secretx33.secretcfg.core.utils

import kotlin.math.max
import kotlin.math.min

object RangeParser {

    fun parseIntOrNull (
        range: String,
        minValue: Int = 0,
        maxValue: Int = Int.MAX_VALUE,
        showErrorMessage: Runnable? = null,
    ) : Pair<Int, Int>? {
        // value is only one value
        SIGNED_INT.matchEntire(range)?.groupValues?.get(1)
            ?.let { min(maxValue, max(minValue, it.toInt())) }
            ?.let { return Pair(it, it) }

        // value is a range of values
        SIGNED_INT_RANGE.matchEntire(range)?.groupValues
            ?.subList(1, 3)
            ?.map { min(maxValue, max(minValue, it.toInt())) }
            ?.let { return Pair(it[0], max(it[0], it[1])) }

        // typed amount is not an integer
        showErrorMessage?.run()
        return null
    }

    fun parseInt(
        range: String,
        default: Int,
        minValue: Int = 0,
        maxValue: Int = Int.MAX_VALUE,
        showErrorMessage: Runnable? = null,
    ): Pair<Int, Int> {
        return parseIntOrNull(range, minValue, maxValue, showErrorMessage) ?: Pair(default, default)
    }

    fun parseDoubleOrNull(
        range: String,
        minValue: Double = 0.0,
        maxValue: Double = Double.MAX_VALUE,
        showErrorMessage: Runnable? = null,
    ): Pair<Double, Double>? {
        // value is only one value
        SIGNED_DOUBLE.matchEntire(range)?.groupValues?.get(1)
            ?.let { min(maxValue, max(minValue, it.toDouble())) }
            ?.let { return Pair(it, it) }

        // value is a range of values
        SIGNED_DOUBLE_RANGE.matchEntire(range)?.groupValues
            ?.subList(1, 3)
            ?.map { min(maxValue, max(minValue, it.toDouble())) }
            ?.let { return Pair(it[0], max(it[0], it[1])) }

        // typed amount is not a double
        showErrorMessage?.run()
        return null
    }

    fun parseDouble(
        range: String,
        default: Double,
        minValue: Double = 0.0,
        maxValue: Double = Double.MAX_VALUE,
        showErrorMessage: Runnable? = null,
    ): Pair<Double, Double> {
        return parseDoubleOrNull(range, minValue, maxValue, showErrorMessage) ?: Pair(default, default)
    }

    // regex matching a range of two integers
    private val SIGNED_INT = """^\s*(-?\d{1,11})\s*$""".toRegex()                                           // "-5"           ->  -5
    private val SIGNED_INT_RANGE = """^\s*(-?\d{1,11}?)\s*-\s*(-?\d{1,11})\s*$""".toRegex()                 // "-5 - -1"      ->  -5 until -1

    // regex matching a range of two doubles
    private val SIGNED_DOUBLE = """^\s*(-?\d+?(?:\.\d+?)?)\s*$""".toRegex()                                 // "-5.0"         ->  -5.0
    private val SIGNED_DOUBLE_RANGE = """^\s*(-?\d+?(?:\.\d+?)?)\s*-\s*(-?\d+?(?:\.\d+)?)\s*$""".toRegex()  // "-5.0 - -1.0"  ->  -5.0 until -1.0
}
