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

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.XPotion
import com.cryptomorin.xseries.XSound
import com.github.secretx33.secretcfg.bukkit.extensions.isAir
import com.github.secretx33.secretcfg.bukkit.parser.ColorParser
import com.github.secretx33.secretcfg.bukkit.parser.ItemSerializer
import com.github.secretx33.secretcfg.bukkit.parser.PatternParser
import com.github.secretx33.secretcfg.core.config.AbstractConfig
import org.bukkit.*
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.logging.Logger

class ConfigImpl (
    plugin: Plugin,
    path: String,
    logger: Logger = plugin.logger,
    copyDefault: Boolean = true,
    filePresentInJar: Boolean = true,
) : AbstractConfig(plugin, plugin.dataFolder, path, logger, copyDefault, filePresentInJar), Config {

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
                    warnInvalidEntry(key, entry)
                    null
                }
            }
        }
    }

    override fun getMaterialSet(key: String, default: Set<Material>, filter: Predicate<Material>): Set<Material> {
        return filteredCachedSet(key, default, filter) { list ->
            list.mapNotNull { entry ->
                XMaterial.matchXMaterial(entry).map { it.parseMaterial() }?.orElse(null) ?: run {
                    warnInvalidEntry(key, entry)
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
                    warnInvalidEntry(key, type)
                    null
                }
            }
        }
    }

    override fun getPotionEffectSet(key: String, default: Set<PotionEffectType>, filter: Predicate<PotionEffectType>): Set<PotionEffectType> {
        return filteredCachedSet(key, default, filter) { set ->
            set.mapNotNull { type ->
                XPotion.parsePotionEffectFromString(type)?.type ?: run {
                    warnInvalidEntry(key, type)
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
                    warnInvalidEntry(key, enchant)
                    null
                }
            }
        }
    }

    override fun getEnchantSet(key: String, default: Set<Enchantment>, filter: Predicate<Enchantment>): Set<Enchantment> {
        return filteredCachedSet(key, default, filter) { set ->
            set.mapNotNull { enchant ->
                XEnchantment.matchXEnchantment(enchant).map { it.parseEnchantment() }?.orElse(null) ?: run {
                    warnInvalidEntry(key, enchant)
                    null
                }
            }
        }
    }

    override fun getItemFlags(key: String, default: Set<ItemFlag>, filter: Predicate<ItemFlag>): Set<ItemFlag>
        = getEnumSet(key, default, ItemFlag::class, filter)

    @Suppress("UNCHECKED_CAST")
    override fun getSound(key: String): Triple<Sound, Float, Float>? {
        cache[key]?.let { runCatching { it as Triple<Sound, Float, Float> }.getOrNull()?.let { return it } }

        val sounds = manager.getString(key, "").split(':', limit = 3).map { it.trim() }
        // no sound
        if(sounds.isEmpty() || sounds[0].isBlank()) return null

        // for "SOUND"
        val sound = XSound.matchXSound(sounds[0]).map { it.parseSound() }?.orElse(null) ?: run {
            warnInvalidEntry(key, sounds[0])
            return null
        }
        if(sounds.size == 1) return Triple(sound, 1f, 1f).also { cache[key] = it }

        // for "SOUND:1.0" as in "SOUND:VOLUME"
        val volume = sounds[1].toDoubleOrNull()?.coerceAtLeast(0.0) ?: run {
            logger.warning("Could not parse volume '${sounds[1]}' of sound '${sound.name}' of key '$key' in file '${file}', please fix your configurations and reload.")
            return Triple(sound, 1f, 1f).also { cache[key] = it }
        }
        if(sounds.size == 2) return Triple(sound, volume.toFloat(), 1f).also { cache[key] = it }

        // for "SOUND:1.0:1.0" as in "SOUND:VOLUME:PITCH"
        val pitch = sounds[2].toDoubleOrNull()?.coerceAtLeast(0.01) ?: run {
            logger.warning("Could not parse pitch '${sounds[2]}' of sound '${sound.name}' of key '$key' in file '${file}', please fix your configurations and reload.")
            return Triple(sound, volume.toFloat(), 1f).also { cache[key] = it }
        }
        return Triple(sound, volume.toFloat(), pitch.toFloat()).also { cache[key] = it }
    }

    override fun getSound(key: String, default: Triple<Sound, Float, Float>): Triple<Sound, Float, Float>
        = getSound(key) ?: default

    override fun getPattern(key: String): Pattern? = manager.getString(key)?.let { parsePattern(it, key) }

    override fun getPattern(key: String, supplier: Supplier<Pattern>): Pattern
        = cachedStringBased(key, supplier) { parsePattern(it, key) }

    override fun getPatternList(key: String, default: List<Pattern>): List<Pattern>
        = cachedList(key, default) { list -> list.mapNotNull { line -> parsePattern(line, key) } }

    private fun parsePattern(line: String, key: String)
        = PatternParser.parsePattern(line,
            invalidPatternLogger = { "[$file] Invalid pattern '$it' for key '$key', please fix your configurations and reload." },
            invalidDyeColorLogger = { pattern, dyeColor -> "[$file] Invalid dye color '$dyeColor' in pattern '$pattern' for key '$key', please fix your configurations and reload." })
}
