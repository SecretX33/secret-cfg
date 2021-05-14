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
package com.github.secretx33.secretcfg.bukkit.serializer

import com.github.secretx33.secretcfg.core.utils.fields
import org.bukkit.Color
import java.util.logging.Logger

class ColorParser(private val fileName: String, private val logger: Logger) {

    fun parse(key: String, color: String, skipBlankString: Boolean = false): Color? {
        if(skipBlankString && color.isBlank()) return null
        val rgbMatch = RGB_COLOR_PATTERN.matchEntire(color)?.groupValues
        // if color to be parsed is RGB (0-255) numbers
        if(rgbMatch != null && rgbMatch.size == 4) {
            return color.toColor(key, rgbMatch)
        }
        // if color is a named color
        return Color::class.fields().entries.firstOrNull { it.key.equals(color, ignoreCase = true) }?.value ?: run {
            logger.warning(MALFORMED_COLOR_STRING.format(fileName, key, color))
            null
        }
    }

    private fun String.toColor(key: String, rgbMatch: List<String>): Color? {
        val r = rgbMatch[1].toInt()
        val g = rgbMatch[2].toInt()
        val b = rgbMatch[3].toInt()
        return try {
            Color.fromRGB(r, g, b)
        } catch(e: IllegalArgumentException) {
            logger.warning(MALFORMED_RGB_COLOR.format(fileName, key, this))
            null
        }
    }

    private companion object {
        const val MALFORMED_COLOR_STRING = "Inside file '%s', seems like you have malformed color string in key '%s', please fix color entry with value '%s' and reload the plugin configurations."
        const val MALFORMED_RGB_COLOR = "Inside file '%s', seems like you have typoed a invalid number in key '%s', more specifically somewhere in '%s', please only use values between 0 and 255 to write the colors."

        // matches three numbers, like "0, 255, 128" and try to convert them to Color
        val RGB_COLOR_PATTERN = """^\s*(\d+?)\s*,\s*(\d+?)\s*,\s*(\d+)\s*$""".toRegex()
    }
}
