package com.github.secretx33.sccfg

import com.github.secretx33.sccfg.manager.IOYamlManager
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

class YamlManager (
    plugin: Plugin,
    path: String,
    logger: Logger = plugin.logger,
    copyDefault: Boolean = true
) : IOYamlManager(plugin, plugin.dataFolder, path, logger, copyDefault) {


}
