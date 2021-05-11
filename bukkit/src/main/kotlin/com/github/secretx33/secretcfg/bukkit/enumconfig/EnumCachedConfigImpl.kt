package com.github.secretx33.secretcfg.bukkit.enumconfig

import com.github.secretx33.secretcfg.bukkit.config.BukkitConfig
import com.github.secretx33.secretcfg.bukkit.config.CachedConfig
import com.github.secretx33.secretcfg.core.enumconfig.AbstractEnumCachedConfig
import com.github.secretx33.secretcfg.core.enumconfig.ConfigEnum
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.logging.Logger
import kotlin.math.log
import kotlin.reflect.KClass

class EnumCachedConfigImpl<U> (
    plugin: Plugin,
    path: String,
    override val configClass: KClass<U>,
    logger: Logger = plugin.logger,
    copyDefault: Boolean = true,
    private val bukkitConfig: CachedConfig = BukkitConfig(plugin, path, logger, copyDefault),
) : CachedConfig by bukkitConfig, EnumCachedConfig<U> where U : ConfigEnum, U : Enum<U> {

    override fun serialize(key: U, item: ItemStack) = serialize(key.name, item)

    override fun deserializeItem(key: U): ItemStack? = deserializeItem(key.name)

    override fun deserializeItem(key: U, default: Supplier<ItemStack>): ItemStack = deserializeItem(key.name, default)

    override fun serialize(key: U, itemList: List<ItemStack>) = serialize(key.name, itemList)

    override fun deserializeItemList(key: U, default: Supplier<List<ItemStack>>): List<ItemStack>
        = deserializeItemList(key.name, default)

    override fun getMaterial(key: U, default: Material): Material
        = getMaterial(key.name, default)

    override fun getMaterialList(key: U, default: List<Material>, filter: Predicate<Material>): List<Material>
        = getMaterialList(key.name, default, filter)

    override fun getMaterialSet(key: U, default: Set<Material>, filter: Predicate<Material>): Set<Material>
        = getMaterialSet(key.name, default, filter)

    override fun getEntityType(key: U, default: EntityType): EntityType
        = getEntityType(key.name, default)

    override fun getEntityTypeList(key: U, default: List<EntityType>, filter: Predicate<EntityType>): List<EntityType>
        = getEntityTypeList(key.name, default, filter)

    override fun getEntityTypeSet(key: U, default: Set<EntityType>, filter: Predicate<EntityType>): Set<EntityType>
        = getEntityTypeSet(key.name, default, filter)

    override fun getPotionEffect(key: U, default: PotionEffectType): PotionEffectType
        = getPotionEffect(key.name, default)

    override fun getPotionEffectList(key: U, default: List<PotionEffectType>, filter: Predicate<PotionEffectType>): List<PotionEffectType>
        = getPotionEffectList(key.name, default, filter)

    override fun getPotionEffectSet(key: U, default: Set<PotionEffectType>, filter: Predicate<PotionEffectType>): Set<PotionEffectType>
        = getPotionEffectSet(key.name, default, filter)

    override fun getParticle(key: U, default: Particle): Particle = getParticle(key.name, default)

    override fun getParticleList(key: U, default: List<Particle>): List<Particle>
        = getParticleList(key.name, default)

    override fun getParticleSet(key: U, default: Set<Particle>): Set<Particle>
        = getParticleSet(key.name, default)

    override fun getColor(key: U, default: Color): Color = getColor(key.name, default)

    override fun getColorList(key: U, default: List<Color>): List<Color>
        = getColorList(key.name, default)

    override fun getColorSet(key: U, default: Set<Color>): Set<Color>
        = getColorSet(key.name, default)

    override fun getDyeColor(key: U, default: DyeColor): DyeColor = getDyeColor(key.name, default)

    override fun getDyeColorList(key: U, default: List<DyeColor>): List<DyeColor>
        = getDyeColorList(key.name, default)

    override fun getDyeColorSet(key: U, default: Set<DyeColor>): Set<DyeColor>
        = getDyeColorSet(key.name, default)

    override fun getEnchant(key: U, default: Enchantment): Enchantment = getEnchant(key.name, default)

    override fun getEnchantList(key: U, default: List<Enchantment>, filter: Predicate<Enchantment>): List<Enchantment>
        = getEnchantList(key.name, default, filter)

    override fun getEnchantSet(key: U, default: Set<Enchantment>, filter: Predicate<Enchantment>): Set<Enchantment>
        = getEnchantSet(key.name, default, filter)
}
