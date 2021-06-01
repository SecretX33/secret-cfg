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
import com.github.secretx33.secretcfg.bukkit.enumconfig.EnumConfig
import com.github.secretx33.secretcfg.bukkit.enumconfig.EnumConfigImpl
import com.github.secretx33.secretcfg.core.config.ConfigOptions
import com.github.secretx33.secretcfg.core.enumconfig.ConfigEnum
import org.bukkit.plugin.Plugin
import java.nio.file.Path
import java.util.logging.Logger
import kotlin.reflect.KClass

/**
 * Used to create configuration for files.
 *
 * @since 1.0
 */
@Suppress("MemberVisibilityCanBePrivate")
object ConfigProvider {

    /**
     * Create an instance of a config file.
     *
     * @param plugin Plugin The instance of the plugin
     * @param path Path The path, starting from dataFolder, where the file is at, including the file
     * @param options ConfigOptions Configuration options can be modified by creating an instance of [ConfigOptions] directly, using [configOptions][ConfigOptions.configOptions] DSL, or by using [ConfigOptions.builder()][ConfigOptions.builder]
     * @return Config The created config file
     * @since 1.0
     */
    fun create(
        plugin: Plugin,
        path: Path,
        options: ConfigOptions = ConfigOptions(),
    ): Config
        = create(plugin, path, plugin.logger, options)

    /**
     * Create an instance of a config file providing a custom logger.
     *
     * @param plugin Plugin The instance of the plugin
     * @param path Path The path, starting from dataFolder, where the file is at, including the file
     * @param logger Logger A custom logger instance
     * @param options ConfigOptions Configuration options can be modified by creating an instance of [ConfigOptions] directly, using [configOptions][ConfigOptions.configOptions] DSL, or by using [ConfigOptions.builder()][ConfigOptions.builder]
     * @return Config The created config file
     * @since 1.0
     */
    fun create(
        plugin: Plugin,
        path: Path,
        logger: Logger,
        options: ConfigOptions = ConfigOptions(),
    ): Config
        = ConfigImpl(plugin, path, logger, options)

    /**
     * Create an instance of an enum config file.
     *
     * @param plugin Plugin The instance of the plugin
     * @param path Path The path, starting from dataFolder, where the file is at, including the file
     * @param configClass KClass<U> The enum config class which have the config keys
     * @param options ConfigOptions Configuration options can be modified by creating an instance of [ConfigOptions] directly, using [configOptions][ConfigOptions.configOptions] DSL, or by using [ConfigOptions.builder()][ConfigOptions.builder]
     * @return EnumConfig<U> The created enum config file
     * @since 1.0
     */
    fun <U> createEnumBased(
        plugin: Plugin,
        path: Path,
        configClass: KClass<U>,
        options: ConfigOptions = ConfigOptions(),
    ): EnumConfig<U> where U : ConfigEnum, U : Enum<U>
        = createEnumBased(plugin, path, configClass, plugin.logger, options)

    /**
     * Create an instance of an enum config file providing a custom logger.
     *
     * @param plugin Plugin The instance of the plugin
     * @param path Path The path, starting from dataFolder, where the file is at, including the file
     * @param configClass KClass<U> The enum config class which have the config keys
     * @param logger Logger A custom logger instance
     * @param options ConfigOptions Configuration options can be modified by creating an instance of [ConfigOptions] directly, using [configOptions][ConfigOptions.configOptions] DSL, or by using [ConfigOptions.builder()][ConfigOptions.builder]
     * @return EnumConfig<U> The created enum config file
     * @since 1.0
     */
    fun <U> createEnumBased(
        plugin: Plugin,
        path: Path,
        configClass: KClass<U>,
        logger: Logger = plugin.logger,
        options: ConfigOptions = ConfigOptions(),
    ): EnumConfig<U> where U : ConfigEnum, U : Enum<U>
        = EnumConfigImpl(plugin, path, configClass, logger, options)
}
