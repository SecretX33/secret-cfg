package com.github.secretx33.sccfg.manager

import java.io.File
import java.util.logging.Logger

abstract class CachedYamlManager (
    private val plugin: Any,
    private val dataFolder: File,
    path: String,
    private val logger: Logger,
    copyDefault: Boolean,
) : IOYamlManager (plugin, dataFolder, path, logger, copyDefault) {


}
