package com.github.secretx33.secretcfg.bukkit.config

import com.github.secretx33.secretcfg.core.config.BaseCachedConfig
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import java.util.function.Predicate
import java.util.function.Supplier

interface CachedConfig : BaseCachedConfig {

    fun serialize(key: String, item: ItemStack)

    fun deserializeItem(key: String): ItemStack?

    fun deserializeItem(key: String, default: Supplier<ItemStack>): ItemStack

    fun serialize(key: String, itemList: List<ItemStack>)

    fun deserializeItemList(key: String, default: Supplier<List<ItemStack>> = Supplier { emptyList() }): List<ItemStack>

    fun getMaterial(key: String, default: Material): Material

    fun getMaterialList(key: String, default: List<Material> = emptyList(), filter: Predicate<Material> = Predicate { true }): List<Material>

    fun getMaterialSet(key: String, default: Set<Material> = emptySet(), filter: Predicate<Material> = Predicate { true }): Set<Material>

    fun getEntityType(key: String, default: EntityType): EntityType

    fun getEntityTypeList(key: String, default: List<EntityType> = emptyList(), filter: Predicate<EntityType> = Predicate { true }): List<EntityType>

    fun getEntityTypeSet(key: String, default: Set<EntityType> = emptySet(), filter: Predicate<EntityType> = Predicate { true }): Set<EntityType>

    fun getPotionEffect(key: String, default: PotionEffectType): PotionEffectType

    fun getPotionEffectList(key: String, default: List<PotionEffectType> = emptyList(), filter: Predicate<PotionEffectType> = Predicate { true }): List<PotionEffectType>

    fun getPotionEffectSet(key: String, default: Set<PotionEffectType> = emptySet(), filter: Predicate<PotionEffectType> = Predicate { true }): Set<PotionEffectType>

    fun getParticle(key: String, default: Particle): Particle

    fun getParticleList(key: String, default: List<Particle> = emptyList()): List<Particle>

    fun getParticleSet(key: String, default: Set<Particle> = emptySet()): Set<Particle>

    fun getColor(key: String, default: Color): Color

    fun getColorList(key: String, default: List<Color> = emptyList()): List<Color>

    fun getColorSet(key: String, default: Set<Color> = emptySet()): Set<Color>

    fun getDyeColor(key: String, default: DyeColor): DyeColor

    fun getDyeColorList(key: String, default: List<DyeColor> = emptyList()): List<DyeColor>

    fun getDyeColorSet(key: String, default: Set<DyeColor> = emptySet()): Set<DyeColor>

    fun getEnchant(key: String, default: Enchantment): Enchantment

    fun getEnchantList(key: String, default: List<Enchantment> = emptyList(), filter: Predicate<Enchantment> = Predicate { true }): List<Enchantment>

    fun getEnchantSet(key: String, default: Set<Enchantment> = emptySet(), filter: Predicate<Enchantment> = Predicate { true }): Set<Enchantment>
}
