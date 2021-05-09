package com.github.secretx33.secretcfg

import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

interface CachedConfig {

    fun serialize(path: String, item: ItemStack)

    fun deserializeItem(path: String): ItemStack?

    fun deserializeItem(path: String, default: ItemStack): ItemStack

    fun serialize(path: String, itemList: List<ItemStack>)

    fun deserializeItemList(path: String, default: List<ItemStack> = emptyList()): List<ItemStack>

    fun getMaterial(path: String, default: Material): Material

    fun getMaterialList(path: String, default: List<Material> = emptyList()): List<Material>

    fun getPotionEffect(path: String, default: PotionEffectType): PotionEffectType

    fun getPotionEffectList(path: String, default: PotionEffectType): List<PotionEffectType>

    fun getParticle(path: String, default: Particle): Particle

    fun getParticleList(path: String, default: List<Particle> = emptyList()): List<Particle>

    fun getColor(path: String, default: Color): Color

    fun getColorList(path: String, default: List<Color> = emptyList()): List<Color>

    fun getDyeColor(path: String, default: DyeColor): DyeColor

    fun getDyeColorList(path: String, default: List<DyeColor> = emptyList()): List<DyeColor>

    fun getEnchant(path: String, default: Color): Enchantment

    fun getEnchantSet(path: String, default: Set<Enchantment> = emptySet()): Set<Enchantment>
}
