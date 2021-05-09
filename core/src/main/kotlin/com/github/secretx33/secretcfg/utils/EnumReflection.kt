package com.github.secretx33.secretcfg.utils

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object EnumReflection {

    private val LOOKUP = MethodHandles.publicLookup()
    private val ARRAY_TYPE = MethodType.methodType(Array::class.java)

    private val enumCache = ConcurrentHashMap<KClass<out Enum<*>>, Array<*>>()

    /**
     * Invokes an enum class `values()` through reflection and return the enum entries
     * in an array.
     *
     * @param clazz KClass<T> enum class that'll get its `values()` method invoked through reflection
     * @return Array<T> array containing all enum entries
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> getValues(clazz: KClass<T>): Array<T> {
        return enumCache.getOrPut(clazz) {
            getValuesHandle(clazz).invokeExact() as Array<*>
        } as Array<T>
    }

    /**
     * Used to get the `values()` method from an enum class, since Kotlin doesn't expose static
     * methods of Enum classes even when a generic class is casted to a generic enum class.
     *
     * @param clazz KClass<T> enum class to get the `values()` method
     * @return MethodHandle the methodhandle of `values()`
     */
    private fun <T : Enum<T>> getValuesHandle(clazz: KClass<T>): MethodHandle {
        return LOOKUP.findStatic(clazz::class.java, "values", ARRAY_TYPE)
    }
}

fun <T : Enum<T>> KClass<out T>.values() = EnumReflection.getValues(this)
