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
package com.github.secretx33.secretcfg.bukkit.parser

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
