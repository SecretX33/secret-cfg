package com.github.secretx33.secretcfg.bukkit.enumconfig

import com.github.secretx33.secretcfg.bukkit.config.Config
import com.github.secretx33.secretcfg.core.enumconfig.BaseEnumConfig
import com.github.secretx33.secretcfg.core.enumconfig.ConfigEnum

interface EnumConfig<U> : BukkitEnumConfig<U>, BaseEnumConfig<U>, Config where U : ConfigEnum, U : Enum<U>
