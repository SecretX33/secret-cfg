package com.github.secretx33.secretcfg.bukkit.enumconfig

import com.github.secretx33.secretcfg.bukkit.config.CachedConfig
import com.github.secretx33.secretcfg.bukkit.config.CachedConfigImpl
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

object BukkitEnumConfig {

    operator fun invoke (
        plugin: Plugin,
        path: String,
        logger: Logger = plugin.logger,
        copyDefault: Boolean = true
    ): CachedConfig {
        return CachedConfigImpl(plugin, path, logger, copyDefault)
    }
}
