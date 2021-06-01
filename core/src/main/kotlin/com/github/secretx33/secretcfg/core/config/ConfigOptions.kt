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
package com.github.secretx33.secretcfg.core.config

/**
 * Holds most config options that can be altered to better adapt to situations.
 *
 * @property copyDefault Boolean If file should be copied from jar when reloading and at start, in case it don't exists
 * @property expectFileInJar Boolean If file is expected to be present in the jar, false will prevent the [IllegalStateException] for missing file and safely ignore the missing file
 * @since 1.0
 */
@Suppress("MemberVisibilityCanBePrivate")
data class ConfigOptions (
    val copyDefault: Boolean = true,
    val expectFileInJar: Boolean = true,
) {

    /**
     * Hybrid Builder/DSL class for [ConfigOptions].
     *
     * @since 1.0
     */
    class Builder internal constructor() {
        var copyDefault: Boolean = true
        var expectFileInJar: Boolean = true

        fun copyDefault(boolean: Boolean): Builder {
            copyDefault = boolean
            return this
        }

        fun expectFileInJar(boolean: Boolean): Builder {
            expectFileInJar = boolean
            return this
        }

        fun build() = ConfigOptions(copyDefault, expectFileInJar)
    }

    companion object {
        /**
         * Creates a new ConfigOptions builder.
         *
         * @return Builder The builder for ConfigOptions
         * @since 1.0
         */
        fun builder(): Builder = Builder()

        /**
         * Creates a new ConfigOptions, configured using DSL.
         *
         * @param block [@kotlin.ExtensionFunctionType] Function1<Builder, Unit> Lambda to apply the desired modifications to your ConfigOptions
         * @return ConfigOptions The configured [ConfigOptions]
         * @since 1.0
         */
        fun configOptions(block: Builder.() -> Unit): ConfigOptions = Builder().apply(block).build()
    }
}
