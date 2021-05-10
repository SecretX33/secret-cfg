package com.github.secretx33.secretcfg.core.utils

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Helper class to make possible get all static fields of a certain type from pseudo-enum classes.
 */
internal object FieldsReflection {

    /**
     * Dummy object to be used to get a class public static fields
     */
    private val OBJ = Any()

    /**
     * Map to cache the field reflection results, stored as Map<Pair<Class, FieldType>, Map<FieldName, FieldContent>>
     */
    private val fieldsCache = ConcurrentHashMap<Pair<KClass<*>, KClass<*>>, Map<String, *>>()

    /**
     * Returns all the public static values of a specific type from a class, mapped as Map<FieldName, FieldContent>.
     *
     * @param clazz [KClass<*>] class to get its public static fields through reflection
     * @param requiredType [KClass<T>] which type of field should be get
     * @return [Map<String, T>] A map containing all the public static fields, mapped to <FieldName, FieldContent>
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValues(clazz: KClass<*>, requiredType: KClass<T>): Map<String, T> {
        return fieldsCache.getOrPut(Pair(clazz, requiredType)) {
            clazz.java.fields.filter { it.type == requiredType.java }
                .associateBy { it.name }
                .mapValues { it.value.get(OBJ) } as Map<String, T>
        } as Map<String, T>
    }
}

//unsafe
fun <T : Any> KClass<T>.fields(requiredType: KClass<T> = this) = FieldsReflection.getValues(this, requiredType)
