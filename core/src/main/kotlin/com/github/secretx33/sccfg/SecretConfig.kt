package com.github.secretx33.sccfg

interface SecretConfig<U : SecretEnum> {

    fun reload()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T): T

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: U): T

    fun <T> get(key: ConfigKeys, default: T): T

    fun getInt(key: String, default: Int, minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Int

    fun getInt(key: ConfigKeys, default: Int = key.defaultValue as Int, minValue: Int = 0, maxValue: Int = Int.MAX_VALUE): Int

    fun getDouble(key: String, default: Double, minValue: Double = 0.0, maxValue: Double = Double.MAX_VALUE): Double

    fun getDouble(key: ConfigKeys, default: Double = key.defaultValue as Double, minValue: Double = 0.0, maxValue: Double = Double.MAX_VALUE): Double

    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> getEnum(key: ConfigKeys): T

    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> getEnumSet(key: ConfigKeys, clazz: Class<out Enum<T>>, predicate: Any? = null): Set<T>

    fun has(path: String): Boolean

    fun set(key: String, value: Any)

    fun set(key: ConfigKeys, value: Any)

    fun save()
}
