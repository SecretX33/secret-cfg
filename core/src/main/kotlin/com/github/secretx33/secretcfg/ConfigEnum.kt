package com.github.secretx33.secretcfg

interface ConfigEnum {
    /**
     * Represents the path of the config
     */
    val path: String

    /**
     * Represent an alternative value in case the key path is non existent
     * or contains an invalid value/type
     */
    val default: Any
}
