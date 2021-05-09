package com.github.secretx33.sccfg

interface SecretEnum {
    /**
     * Represents the path of the config
     */
    val path: String

    /**
     * Represent an alternative value in case the key path is non existent
     */
    val defaultValue: Any
}
