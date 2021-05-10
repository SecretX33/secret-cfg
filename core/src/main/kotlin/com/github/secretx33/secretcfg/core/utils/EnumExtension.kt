package com.github.secretx33.secretcfg.core.utils

import kotlin.reflect.KClass

/**
 * Extension method to give generic enum classes access to the static method [Enum#values()][Enum.values()].
 *
 * @since 1.0
 */
internal fun <T : Enum<T>> KClass<out T>.values() = java.enumConstants
