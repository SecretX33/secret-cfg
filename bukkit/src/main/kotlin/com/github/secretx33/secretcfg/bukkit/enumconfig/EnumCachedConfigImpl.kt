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

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.XPotion
import com.github.secretx33.secretcfg.bukkit.config.BukkitConfig
import com.github.secretx33.secretcfg.bukkit.config.CachedConfig
import com.github.secretx33.secretcfg.bukkit.extensions.isAir
import com.github.secretx33.secretcfg.bukkit.serializer.ColorParser
import com.github.secretx33.secretcfg.bukkit.serializer.ItemSerializer
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
) : AbstractEnumCachedConfig<U>(plugin, plugin.dataFolder, configClass, path, logger, copyDefault),
    EnumCachedConfig<U> where U : ConfigEnum, U : Enum<U> {

    private val colorParser = ColorParser(manager.fileName, logger)

    // ItemStack Serialization & Deserialization

    override fun serialize(key: String, item: ItemStack) = set(key, ItemSerializer.toString(item))

    override fun deserializeItem(key: String): ItemStack? {
        val item = getString(key, "").takeIf { it.isNotBlank() } ?: return null
        return ItemSerializer.fromString(item)
    }

    override fun deserializeItem(key: String, default: Supplier<ItemStack>): ItemStack {
        val item = getString(key, "").takeIf { it.isNotBlank() } ?: return default.get()
        return ItemSerializer.fromStringOrNull(item) ?: default.get()
    }

    override fun serialize(key: String, itemList: List<ItemStack>) {
        itemList.filter { !it.isAir && it.type.isItem }
            .map { ItemSerializer.toString(it) }
            .let { set(key, it) }
    }

    override fun deserializeItemList(key: String, default: Supplier<List<ItemStack>>): List<ItemStack> {
        return cachedList(key, default) { list ->
            list.mapNotNull { ItemSerializer.fromStringOrNull(it) }
        }
    }

    // Material

    override fun getMaterial(key: String, default: Material): Material
            = cachedEnum(key, default) { enum -> XMaterial.matchXMaterial(enum).map { it.parseMaterial() }?.orElse(null) }

    override fun getMaterialList(key: String, default: List<Material>, filter: Predicate<Material>): List<Material> {
        return filteredCachedList(key, default, filter) { list ->
            list.mapNotNull { entry ->
                XMaterial.matchXMaterial(entry).map { it.parseMaterial() }?.orElse(null) ?: run {
                    warnInvalidEnumEntry(key, entry)
                    null
                }
            }
        }
    }

    override fun getMaterialSet(key: String, default: Set<Material>, filter: Predicate<Material>): Set<Material> {
        return filteredCachedSet(key, default, filter) { list ->
            list.mapNotNull { entry ->
                XMaterial.matchXMaterial(entry).map { it.parseMaterial() }?.orElse(null) ?: run {
                    warnInvalidEnumEntry(key, entry)
                    null
                }
            }
        }
    }

    // EntityType

    override fun getEntityType(key: String, default: EntityType): EntityType
            = cachedEnum(key, default) { type -> EntityType.values().firstOrNull { it.name.equals(type, ignoreCase = true) } }

    override fun getEntityTypeList(key: String, default: List<EntityType>, filter: Predicate<EntityType>): List<EntityType>
            = getEnumList(key, default, EntityType::class, filter)

    override fun getEntityTypeSet(key: String, default: Set<EntityType>, filter: Predicate<EntityType>): Set<EntityType>
            = getEnumSet(key, default, EntityType::class, filter)


    // Potion Effect Type

    override fun getPotionEffect(key: String, default: PotionEffectType): PotionEffectType
            = cachedStringBased(key, default) { type -> XPotion.parsePotionEffectFromString(type)?.type }

    override fun getPotionEffectList(key: String, default: List<PotionEffectType>, filter: Predicate<PotionEffectType>): List<PotionEffectType> {
        return filteredCachedList(key, default, filter) { list ->
            list.mapNotNull { type ->
                XPotion.parsePotionEffectFromString(type)?.type ?: run {
                    warnInvalidEnumEntry(key, type)
                    null
                }
            }
        }
    }

    override fun getPotionEffectSet(key: String, default: Set<PotionEffectType>, filter: Predicate<PotionEffectType>): Set<PotionEffectType> {
        return filteredCachedSet(key, default, filter) { set ->
            set.mapNotNull { type ->
                XPotion.parsePotionEffectFromString(type)?.type ?: run {
                    warnInvalidEnumEntry(key, type)
                    null
                }
            }
        }
    }


    // Particle

    override fun getParticle(key: String, default: Particle): Particle
            = cachedEnum(key, default) { enum -> Particle.values().firstOrNull { it.name.equals(enum, ignoreCase = true) } }

    override fun getParticleList(key: String, default: List<Particle>): List<Particle>
            = getEnumList(key, default, Particle::class)

    override fun getParticleSet(key: String, default: Set<Particle>): Set<Particle>
            = getEnumSet(key, default, Particle::class)


    // Color

    override fun getColor(key: String, default: Color): Color
            = cachedStringBased(key, default) { color -> colorParser.parse(key, color) }

    // parse the color entries, warning about invalid colors
    override fun getColorList(key: String, default: List<Color>): List<Color>
            = cachedList(key, default) { list -> list.mapNotNull { colorParser.parse(key, it) } }

    // parse the color entries, warning about invalid colors
    override fun getColorSet(key: String, default: Set<Color>): Set<Color>
            = cachedSet(key, default) { list -> list.mapNotNullTo(mutableSetOf()) { colorParser.parse(key, it) } }


    // Dye Color

    override fun getDyeColor(key: String, default: DyeColor): DyeColor
            = cachedEnum(key, default) { enum -> DyeColor.values().firstOrNull { it.name.equals(enum, ignoreCase = true) }  }

    override fun getDyeColorList(key: String, default: List<DyeColor>): List<DyeColor>
            = getEnumList(key, default, DyeColor::class)


    override fun getDyeColorSet(key: String, default: Set<DyeColor>): Set<DyeColor>
            = getEnumSet(key, default, DyeColor::class)

    // Enchantment

    override fun getEnchant(key: String, default: Enchantment): Enchantment
            = cachedStringBased(key, default) { enchant -> XEnchantment.matchXEnchantment(enchant).map { it.parseEnchantment() }?.orElse(null) }

    override fun getEnchantList(key: String, default: List<Enchantment>, filter: Predicate<Enchantment>): List<Enchantment> {
        return filteredCachedList(key, default, filter) { list ->
            list.mapNotNull { enchant ->
                XEnchantment.matchXEnchantment(enchant).map { it.parseEnchantment() }?.orElse(null) ?: run {
                    warnInvalidEnumEntry(key, enchant)
                    null
                }
            }
        }
    }

    override fun getEnchantSet(key: String, default: Set<Enchantment>, filter: Predicate<Enchantment>): Set<Enchantment> {
        return filteredCachedSet(key, default, filter) { set ->
            set.mapNotNull { enchant ->
                XEnchantment.matchXEnchantment(enchant).map { it.parseEnchantment() }?.orElse(null) ?: run {
                    warnInvalidEnumEntry(key, enchant)
                    null
                }
            }
        }
    }

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
