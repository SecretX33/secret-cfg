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
package com.github.secretx33.secretcfg.core.util

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
