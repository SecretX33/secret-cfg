package com.github.secretx33.secretcfg.core.utils

import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object FieldsReflection {

    private val OBJ = Any()
    private val fieldsCache = ConcurrentHashMap<KClass<*>, Map<String, *>>()

    /**
     * Returns all the public values from a specific class type Invokes an enum class `values()` through reflection and return the enum entries
     * in an array.
     *
     * @param clazz KClass<T> enum class that'll get its `values()` method invoked through reflection
     * @return Array<T> array containing all enum entries
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValues(clazz: KClass<*>, requiredType: KClass<T>): Map<String, T> {
        return fieldsCache.getOrPut(clazz) {
            clazz.java.fields.filter { it.type == requiredType.java }
                .associateBy { it.name }
                .mapValues { it.value.get(OBJ) } as Map<String, T>
        } as Map<String, T>
    }
}

//unsafe
fun <T : Any> KClass<T>.fields(requiredType: KClass<T> = this) = FieldsReflection.getValues(this, requiredType)
