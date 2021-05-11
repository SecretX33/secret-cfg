package com.github.secretx33.secretcfg.bukkit.config

import org.bukkit.plugin.Plugin
import java.util.logging.Logger

object BukkitConfig {

    operator fun invoke (
        plugin: Plugin,
        path: String,
        logger: Logger = plugin.logger,
        copyDefault: Boolean = true
    ): CachedConfig {
        return CachedConfigImpl(plugin, path, logger, copyDefault)
    }
}
