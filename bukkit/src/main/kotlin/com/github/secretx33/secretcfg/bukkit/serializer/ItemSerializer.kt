package com.github.secretx33.secretcfg.bukkit.serializer

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

internal object ItemSerializer {

    fun toString(item: ItemStack): String {
        ByteArrayOutputStream().use { os ->
            BukkitObjectOutputStream(os).use { data ->
                data.writeObject(item)
                return Base64.getEncoder().withoutPadding().encodeToString(os.toByteArray())
            }
        }
    }

    fun fromString(item: String): ItemStack {
        ByteArrayInputStream(Base64.getDecoder().decode(item)).use { stream ->
            BukkitObjectInputStream(stream).use { data ->
                return data.readObject() as ItemStack
            }
        }
    }

    fun fromStringOrNull(item: String): ItemStack? {
        return runCatching {
            ByteArrayInputStream(Base64.getDecoder().decode(item)).use { stream ->
                BukkitObjectInputStream(stream).use { data ->
                    return data.readObject() as? ItemStack
                }
            }
        }.getOrNull()
    }
}
