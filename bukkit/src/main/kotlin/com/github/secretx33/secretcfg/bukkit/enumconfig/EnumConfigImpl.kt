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
package com.github.secretx33.secretcfg.bukkit.enumconfig

import com.github.secretx33.secretcfg.bukkit.config.BukkitConfig
import com.github.secretx33.secretcfg.bukkit.config.Config
import com.github.secretx33.secretcfg.bukkit.config.ConfigImpl
import com.github.secretx33.secretcfg.core.config.ConfigOptions
import com.github.secretx33.secretcfg.core.enumconfig.AbstractEnumConfig
import com.github.secretx33.secretcfg.core.enumconfig.ConfigEnum
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import java.nio.file.Path
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.logging.Logger
import kotlin.reflect.KClass

class EnumConfigImpl<U> (
    plugin: Plugin,
    path: Path,
    override val configClass: KClass<U>,
    logger: Logger,
    options: ConfigOptions,
    private val bukkitConfig: Config = ConfigImpl(plugin, path, logger, options),
) : AbstractEnumConfig<U>(configClass, bukkitConfig),
    BukkitConfig by bukkitConfig,
    EnumConfig<U> where U : ConfigEnum, U : Enum<U> {

    override fun serialize(key: U, item: ItemStack) = serialize(key.path, item)

    override fun deserializeItem(key: U): ItemStack? = deserializeItem(key.path)

    override fun deserializeItem(key: U, default: Supplier<ItemStack>): ItemStack = deserializeItem(key.path, default)

    override fun serialize(key: U, itemList: List<ItemStack>) = serialize(key.path, itemList)

    override fun deserializeItemList(key: U, default: Supplier<List<ItemStack>>): List<ItemStack>
        = deserializeItemList(key.path, default)

    override fun getMaterial(key: U, default: Material): Material
        = getMaterial(key.path, default)

    override fun getMaterialList(key: U, default: List<Material>, filter: Predicate<Material>): List<Material>
        = getMaterialList(key.path, default, filter)

    override fun getMaterialSet(key: U, default: Set<Material>, filter: Predicate<Material>): Set<Material>
        = getMaterialSet(key.path, default, filter)

    override fun getEntityType(key: U, default: EntityType): EntityType
        = getEntityType(key.path, default)

    override fun getEntityTypeList(key: U, default: List<EntityType>, filter: Predicate<EntityType>): List<EntityType>
        = getEntityTypeList(key.path, default, filter)

    override fun getEntityTypeSet(key: U, default: Set<EntityType>, filter: Predicate<EntityType>): Set<EntityType>
        = getEntityTypeSet(key.path, default, filter)

    override fun getPotionEffect(key: U, default: PotionEffectType): PotionEffectType
        = getPotionEffect(key.path, default)

    override fun getPotionEffectList(key: U, default: List<PotionEffectType>, filter: Predicate<PotionEffectType>): List<PotionEffectType>
        = getPotionEffectList(key.path, default, filter)

    override fun getPotionEffectSet(key: U, default: Set<PotionEffectType>, filter: Predicate<PotionEffectType>): Set<PotionEffectType>
        = getPotionEffectSet(key.path, default, filter)

    override fun getParticle(key: U, default: Particle): Particle = getParticle(key.path, default)

    override fun getParticleList(key: U, default: List<Particle>): List<Particle>
        = getParticleList(key.path, default)

    override fun getParticleSet(key: U, default: Set<Particle>): Set<Particle>
        = getParticleSet(key.path, default)

    override fun getColor(key: U, default: Color): Color = getColor(key.path, default)

    override fun getColorList(key: U, default: List<Color>): List<Color>
        = getColorList(key.path, default)

    override fun getColorSet(key: U, default: Set<Color>): Set<Color>
        = getColorSet(key.path, default)

    override fun getDyeColor(key: U, default: DyeColor): DyeColor = getDyeColor(key.path, default)

    override fun getDyeColorList(key: U, default: List<DyeColor>): List<DyeColor>
        = getDyeColorList(key.path, default)

    override fun getDyeColorSet(key: U, default: Set<DyeColor>): Set<DyeColor>
        = getDyeColorSet(key.path, default)

    override fun getEnchant(key: U, default: Enchantment): Enchantment = getEnchant(key.path, default)

    override fun getEnchantList(key: U, default: List<Enchantment>, filter: Predicate<Enchantment>): List<Enchantment>
        = getEnchantList(key.path, default, filter)

    override fun getEnchantSet(key: U, default: Set<Enchantment>, filter: Predicate<Enchantment>): Set<Enchantment>
        = getEnchantSet(key.path, default, filter)

    override fun getItemFlags(key: U, default: Set<ItemFlag>, filter: Predicate<ItemFlag>): Set<ItemFlag>
        = getItemFlags(key.path, default, filter)

    override fun getSound(key: U): Triple<Sound, Float, Float>? = getSound(key.path)

    override fun getSound(key: U, default: Triple<Sound, Float, Float>): Triple<Sound, Float, Float>
        = getSound(key) ?: default

    override fun getPattern(key: U, default: Supplier<Pattern>): Pattern = getPattern(key.path, default)

    override fun getPatternList(key: U, default: List<Pattern>): List<Pattern> = getPatternList(key.path, default)
}
