package com.github.secretx33.secretcfg.core.exception

open class ConfigException(message: String) : RuntimeException(message)

/**
 * Thrown when parameter used as default in ConfigEnum is not from the required type T
 */
class InvalidDefaultParameterException(message: String) : ConfigException(message)

/**
 * Exception that will be thrown when consumer tries to get a value, but the cached value under the same key is from a different type.
 */
class DifferentCachedTypeException(message: String) : ConfigException(message)

