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
package com.github.secretx33.secretcfg.bukkit.config

import org.bukkit.*
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import java.util.function.Predicate
import java.util.function.Supplier

interface BukkitConfig {

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

    fun getItemFlags(key: String, default: Set<ItemFlag> = emptySet(), filter: Predicate<ItemFlag> = Predicate { true }): Set<ItemFlag>

    /**
     * Parse and return a sound string Triple containing <Sound, Volume, Pitch>. String should be formatted as "Sound",
     * "Sound:Volume" or "Sound:Volume:Pitch".
     *
     * @param key String Where the sound is located at
     * @return Triple<Sound, Float, Float>? The parsed sound if entry was found and contained a valid sound name, or null if the entry was missing or the sound part was invalid
     */
    fun getSound(key: String): Triple<Sound, Float, Float>?

    /**
     * Parse and return a sound string Triple containing <Sound, Volume, Pitch>. String should be formatted as "Sound",
     * "Sound:Volume" or "Sound:Volume:Pitch".
     *
     * @param key String Where the sound is located at
     * @param default Triple<Sound, Float, Float> A default value in case the default entry is invalid or missing
     * @return Triple<Sound, Float, Float> The parsed sound if entry was found and contained a valid sound name, or the default value otherwise
     */
    fun getSound(key: String, default: Triple<Sound, Float, Float>): Triple<Sound, Float, Float>

    fun getPattern(key: String): Pattern?

    fun getPattern(key: String, supplier: Supplier<Pattern>): Pattern

    fun getPatternList(key: String, default: List<Pattern> = emptyList()): List<Pattern>
}
