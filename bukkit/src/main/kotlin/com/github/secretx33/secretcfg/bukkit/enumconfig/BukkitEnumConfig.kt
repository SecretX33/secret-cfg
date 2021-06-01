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

import com.github.secretx33.secretcfg.core.enumconfig.ConfigEnum
import com.github.secretx33.secretcfg.core.exception.InvalidDefaultParameterException
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
import org.bukkit.potion.PotionEffectType
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.reflect.KClass

interface BukkitEnumConfig<U> where U : ConfigEnum, U : Enum<U> {

    val file: String

    val configClass: KClass<U>

    private inline fun <reified T> U.safeDefault(): T = runCatching { default as T }.getOrElse { throw InvalidDefaultParameterException("Default parameter provided for config key '$name' is not from the expected type '${T::class::simpleName}', but instead its type is '${default::class.simpleName}', please fix your default parameter of this key from your ${configClass.simpleName} class of file '$file'. If you are seeing this error and are not the developer, you should copy this message and send it to they so they can fix the issue.") }

    fun serialize(key: U, item: ItemStack)

    fun deserializeItem(key: U): ItemStack?

    fun deserializeItem(key: U, default: Supplier<ItemStack>): ItemStack

    fun serialize(key: U, itemList: List<ItemStack>)

    fun deserializeItemList(key: U, default: Supplier<List<ItemStack>> = Supplier { emptyList() }): List<ItemStack>

    fun getMaterial(key: U, default: Material = key.safeDefault()): Material

    fun getMaterialList(key: U, default: List<Material> = key.safeDefault(), filter: Predicate<Material> = Predicate { true }): List<Material>

    fun getMaterialSet(key: U, default: Set<Material> = key.safeDefault(), filter: Predicate<Material> = Predicate { true }): Set<Material>

    fun getEntityType(key: U, default: EntityType = key.safeDefault()): EntityType

    fun getEntityTypeList(key: U, default: List<EntityType> = key.safeDefault(), filter: Predicate<EntityType> = Predicate { true }): List<EntityType>

    fun getEntityTypeSet(key: U, default: Set<EntityType> = key.safeDefault(), filter: Predicate<EntityType> = Predicate { true }): Set<EntityType>

    fun getPotionEffect(key: U, default: PotionEffectType = key.safeDefault()): PotionEffectType

    fun getPotionEffectList(key: U, default: List<PotionEffectType> = key.safeDefault(), filter: Predicate<PotionEffectType> = Predicate { true }): List<PotionEffectType>

    fun getPotionEffectSet(key: U, default: Set<PotionEffectType> = key.safeDefault(), filter: Predicate<PotionEffectType> = Predicate { true }): Set<PotionEffectType>

    fun getParticle(key: U, default: Particle = key.safeDefault()): Particle

    fun getParticleList(key: U, default: List<Particle> = key.safeDefault()): List<Particle>

    fun getParticleSet(key: U, default: Set<Particle> = key.safeDefault()): Set<Particle>

    fun getColor(key: U, default: Color = key.safeDefault()): Color

    fun getColorList(key: U, default: List<Color> = key.safeDefault()): List<Color>

    fun getColorSet(key: U, default: Set<Color> = key.safeDefault()): Set<Color>

    fun getDyeColor(key: U, default: DyeColor = key.safeDefault()): DyeColor

    fun getDyeColorList(key: U, default: List<DyeColor> = key.safeDefault()): List<DyeColor>

    fun getDyeColorSet(key: U, default: Set<DyeColor> = key.safeDefault()): Set<DyeColor>

    fun getEnchant(key: U, default: Enchantment = key.safeDefault()): Enchantment

    fun getEnchantList(key: U, default: List<Enchantment> = key.safeDefault(), filter: Predicate<Enchantment> = Predicate { true }): List<Enchantment>

    fun getEnchantSet(key: U, default: Set<Enchantment> = key.safeDefault(), filter: Predicate<Enchantment> = Predicate { true }): Set<Enchantment>

    fun getItemFlags(key: U, default: Set<ItemFlag> = key.safeDefault(), filter: Predicate<ItemFlag> = Predicate { true }): Set<ItemFlag>

    /**
     * Parse and return a sound string Triple containing <Sound, Volume, Pitch>. String should be formatted as "Sound",
     * "Sound:Volume" or "Sound:Volume:Pitch".
     *
     * @param key U Where the sound is located at
     * @return Triple<Sound, Float, Float>? The parsed sound if entry was found and contained a valid sound name
     */
    fun getSound(key: U): Triple<Sound, Float, Float>?

    /**
     * Parse and return a sound string Triple containing <Sound, Volume, Pitch>. String should be formatted as "Sound",
     * "Sound:Volume" or "Sound:Volume:Pitch".
     *
     * @param key U Where the sound is located at
     * @param default Triple<Sound, Float, Float>The parsed sound if entry was found and contained a valid sound name
     * @return Triple<Sound, Float, Float>? The parsed sound if entry was found and contained a valid sound name, or the default value otherwise
     */
    fun getSound(key: U, default: Triple<Sound, Float, Float> = key.safeDefault()): Triple<Sound, Float, Float>

    fun getPattern(key: U, default: Supplier<Pattern> = key.safeDefault()): Pattern

    fun getPatternList(key: U, default: List<Pattern> = key.safeDefault()): List<Pattern>
}
