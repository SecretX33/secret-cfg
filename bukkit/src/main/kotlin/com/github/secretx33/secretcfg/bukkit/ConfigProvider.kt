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
package com.github.secretx33.secretcfg.bukkit

import com.github.secretx33.secretcfg.bukkit.config.Config
import com.github.secretx33.secretcfg.bukkit.config.ConfigImpl
import com.github.secretx33.secretcfg.bukkit.enumconfig.BukkitEnumConfig
import com.github.secretx33.secretcfg.bukkit.enumconfig.EnumConfig
import com.github.secretx33.secretcfg.bukkit.enumconfig.EnumConfigImpl
import com.github.secretx33.secretcfg.core.enumconfig.ConfigEnum
import org.bukkit.plugin.Plugin
import java.util.logging.Logger
import kotlin.reflect.KClass

object ConfigProvider {

    fun create(
        plugin: Plugin,
        path: String,
        logger: Logger = plugin.logger,
        copyDefault: Boolean = true,
        filePresentInJar: Boolean = true,
    ): Config {
        return ConfigImpl(plugin, path, logger, copyDefault, filePresentInJar)
    }

    fun <U> createEnumBased(
        plugin: Plugin,
        path: String,
        configClass: KClass<U>,
        logger: Logger = plugin.logger,
        copyDefault: Boolean = true,
        filePresentInJar: Boolean = true,
    ): EnumConfig<U> where U : ConfigEnum, U : Enum<U> {
        return EnumConfigImpl(plugin, path, configClass, logger, copyDefault, filePresentInJar)
    }
}
