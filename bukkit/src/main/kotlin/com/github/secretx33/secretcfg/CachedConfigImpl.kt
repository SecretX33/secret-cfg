package com.github.secretx33.secretcfg

import com.github.secretx33.secretcfg.core.BaseCachedConfig
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

class CachedConfigImpl (
    plugin: Plugin,
    path: String,
    logger: Logger = plugin.logger,
    copyDefault: Boolean = true
) : BaseCachedConfig(plugin, plugin.dataFolder, path, logger, copyDefault) {

    fun serialize(path: String, item: ItemStack) {
        set(path, item.serialize())
    }

    fun deserializeItem(path: String): ItemStack? {

    }

    fun deserializeItem(path: String, default: ItemStack): ItemStack {

    }

    fun getMaterial(path: String): Material? {

    }

    fun getMaterial(path: String, default: Material): Material {

    }

    fun getMaterialList(path: String): List<Material> {

    }

    fun getMaterialSet(path: String): Set<Material> {

    }

    fun getMaterial(path: String): Set<Material> {

    }

}
