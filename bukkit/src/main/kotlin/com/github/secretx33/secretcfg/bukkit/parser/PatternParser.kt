package com.github.secretx33.secretcfg.bukkit.parser

import org.bukkit.DyeColor
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import java.util.function.BiConsumer
import java.util.function.Consumer

object PatternParser {

    /**
     * Parse a string encoded banner (and shield) pattern such as "half_vertical:magenta" ("Enum:Dye_Color") or "sku:RED" ("Enum:Dye_Color").
     * Accepts both the Pattern enum name or its identifier as the first part, and a Dye Color as the second part.
     *
     * @param line String A pattern formatted as "pattern:dye_color", case insensitive
     * @param invalidPatternLogger Consumer<String>? A logger to print an error message to the user when this function parses an invalid pattern
     * @param invalidDyeColorLogger BiConsumer<String, String>? A logger to print an error message to the user when this function parses an invalid dye color, first string is the Pattern, second string is the invalid dye color
     * @return Pattern? If the input had a least a valid pattern, or null if there was no valid pattern in the string
     */
    fun parsePattern(
        line: String,
        invalidPatternLogger: Consumer<String>? = null,
        invalidDyeColorLogger: BiConsumer<String, String>? = null,
    ): Pattern? {
        val split = line.split(':', limit = 2).takeIf { it.isNotEmpty() }?.map { it.trim() } ?: return null

        val pattern = PatternType.values().firstOrNull { it.name.equals(split[0], ignoreCase = true) || it.identifier.equals(split[0], ignoreCase = true) }
            ?: run {
                invalidPatternLogger?.accept(split[0])
                return null
            }
        if (split.size == 1) return Pattern(DyeColor.WHITE, pattern)

        val dyeColor = DyeColor.values().firstOrNull { it.name.equals(split[1], ignoreCase = true) } ?: run {
            invalidDyeColorLogger?.accept(split[0], split[1])
            return Pattern(DyeColor.WHITE, pattern)
        }
        return Pattern(dyeColor, pattern)
    }
}
