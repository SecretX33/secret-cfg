package com.github.secretx33.secretcfg.bukkit.serializer

import com.github.secretx33.secretcfg.core.utils.fields
import org.bukkit.Color
import java.util.logging.Logger

class ColorParser(private val fileName: String, private val logger: Logger) {

    fun parse(key: String, color: String): Color? {
        val rgbMatch = COLOR_PATTERN.matchEntire(color)?.groupValues
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
        const val MALFORMED_RGB_COLOR = "Inside file '%s', seems like you have typoed a invalid number in key '%s', more especifically somewhere in '%s', please only use values between 0 and 255 to write the colors."

        // matches three numbers, like "0, 255, 128" and try to convert them to Color
        val COLOR_PATTERN = """^\s*(\d+?),\s*(\d+?),\s*(\d+)\s*$""".toRegex()
    }
}
