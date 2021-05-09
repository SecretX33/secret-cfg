package com.github.secretx33.secretcfg.bukkit.extensions

import org.bukkit.inventory.ItemStack

internal val ItemStack.isAir
    get() = type.isItem
