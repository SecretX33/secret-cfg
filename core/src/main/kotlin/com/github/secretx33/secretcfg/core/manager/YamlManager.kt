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
package com.github.secretx33.secretcfg.core.manager

import com.github.secretx33.secretcfg.core.config.ConfigOptions
import com.github.secretx33.secretcfg.core.storage.FileWatcherProvider
import com.github.secretx33.secretcfg.core.storage.filewatcher.FileModificationType
import com.github.secretx33.secretcfg.core.storage.filewatcher.FileWatcherEvent
import com.github.secretx33.secretcfg.core.util.extension.nameEndsWithAny
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.io.path.writeBytes
import kotlin.math.max

/**
 * Class responsive to manage low level IO operations to the file, as well
 * as saving and keeping the commentaries, it must work on all platforms.
 *
 * @property plugin Any An instance of the plugin's class
 * @property dataFolder Path Folder where the configurations must be placed at
 * @property logger Logger An instance of logger
 * @property path Path The path to file, starting from the dataFolder
 * @property options ConfigOptions Options of this manager
 * @property relativePath Path The relative path to file, starting from the dataFolder
 * @property file Path The full, absolute path to the file
 * @property fileName String Extension function to get the name of the file
 */
class YamlManager (
    private val plugin: Any,
    private val dataFolder: Path,
    path: Path,
    private val logger: Logger,
    private val options: ConfigOptions,
) {
    val relativePath: Path = if(path.nameEndsWithAny(".yml", ".yaml")) path else Path("$path.yml")
    val file: Path = dataFolder.resolve(relativePath).absolute()

    private val saveLock = Mutex()
    private val loader = newYamlLoader()
    private lateinit var root: CommentedConfigurationNode

    private val watcher by lazy { FileWatcherProvider.get(dataFolder).getWatcher(relativePath) }

    val fileName: String
        get() = relativePath.name

    init { runBlocking { reload() } }

    suspend fun reload(): Boolean = withContext(Dispatchers.IO) {
        try {
            file.createIfMissing()
            root = loader.load()
            true
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Error while reloading file '$fileName'", e)
            false
        }
    }

    fun listener (
        modificationType: Set<FileModificationType> = FileModificationType.CREATE_AND_MODIFICATION,
        scope: CoroutineDispatcher = Dispatchers.Default,
        listener: suspend (FileWatcherEvent) -> Unit
    ) = watcher.addListener(modificationType, scope,listener)

    private fun Path.createIfMissing() {
        if(exists()) return

        parent?.createDirectories()
        createFile()
        if(!options.copyDefault) return

        val internalFile: InputStream = plugin.javaClass.classLoader.getResourceAsStream(relativePath.toString())
            ?: plugin.javaClass.classLoader.getResourceAsStream(name)
            ?: if(options.expectFileInJar) throw IllegalStateException("resource '$fileName' was not found in jar") else return

        writeBytes(internalFile.readBytes())
    }

    fun get(key: String): Any? = root.parseNode(key).get(Any::class.java)

    fun get(key: String, default: Any): Any = get(key) ?: default

    fun getBoolean(key: String): Boolean? = root.parseNode(key).get(Boolean::class.java)

    fun getBoolean(key: String, default: Boolean): Boolean = root.parseNode(key).getBoolean(default)

    fun getInt(key: String): Int? = root.parseNode(key).get(Int::class.java)

    fun getInt(key: String, default: Int): Int = root.parseNode(key).getInt(default)

    fun getFloat(key: String): Float? = root.parseNode(key).get(Float::class.java)

    fun getFloat(key: String, default: Float): Float = root.parseNode(key).getFloat(default)

    fun getDouble(key: String): Double? = root.parseNode(key).get(Double::class.java)

    fun getDouble(key: String, default: Double): Double = root.parseNode(key).getDouble(default)

    fun getString(key: String): String? = root.parseNode(key).string

    fun getString(key: String, default: String): String = root.parseNode(key).getString(default)

    fun getStringList(key: String, default: List<String> = emptyList()): List<String>
        = root.parseNode(key).getList(String::class.java) ?: default

    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String>
        = root.parseNode(key).getList(String::class.java)?.toSet() ?: default

    fun set(key: String, value: Any) { root.parseNode(key).set(value) }

    fun setBoolean(key: String, value: Boolean) { root.parseNode(key).set(Boolean::class.java, value) }

    fun setInt(key: String, value: Int) { root.parseNode(key).set(Int::class.java, value) }

    fun setDouble(key: String, value: Double) { root.parseNode(key).set(Double::class.java, value) }

    fun setString(key: String, value: String) { root.parseNode(key).set(String::class.java, value) }

    fun setStringList(key: String, value: Collection<String>) {
        val list = when(value) {
            is List<String> -> value
            else -> value.toList()
        }
        root.parseNode(key).setList(String::class.java, list)
    }

    fun getKeys(): Set<String> = root.childrenMap().keys.mapNotNull { it.toString() }.toSet()

    fun getKeys(path: String): Set<String> = root.parseNode(path).childrenMap().keys.mapNotNull { it.toString() }.toSet()

    fun contains(key: String): Boolean = get(key) != null

    private fun CommentedConfigurationNode.parseNode(path: String) = node(path.split('.'))

    suspend fun save() = withContext(Dispatchers.IO) {
        saveLock.withLock {
            watcher.recordChange(relativePath)
            try {
                file.createIfMissing()
                val oldFile = file.getLines()
                val comments = parseFileComments(oldFile)
                // commit all changes made to the file, erasing the comments in the process
                @Suppress("BlockingMethodInNonBlockingContext")
                loader.save(root)
                // re-add comments to the file
                val newFile = addCommentsToFile(comments)
                // and write the file on the disk
                file.writeLines(newFile)
            } catch (e: Exception) {
                logger.log(Level.WARNING, "Error while saving file $fileName", e)
            }
        }
    }

    private fun getFullKeyOfLine(index: Int, lines: List<String>): String {
        if(index < 0 || index >= lines.size) return ""

        val line = lines[index]
        val key = StringBuilder()
        val depth = lineDepth(line)
        var keyIsFromList = false

        key.append(
            KEY_PATTERN.matchOrNull(line, 1)
                ?: LIST_PATTERN.matchOrNull(line, 1)?.replace("-", "")?.trim().also { keyIsFromList = true }
                ?: return "")

        if(depth <= 0 || index == 0) return key.toString()

        for(i in (index - 1) downTo 0) {
            if(lines[i].isBlank() || COMMENT_PATTERN.matcher(lines[i]).matches()) continue
            val subDepth = lineDepth(lines[i])
            // if subKey has less depth than the original key, it means that this key is its parent
            // the list part is here because yaml is cancer and align the list entries with its parent key
            if(subDepth < depth || keyIsFromList && depth == subDepth && KEY_PATTERN.matches(lines[i])) {
                key.insert(0, '.')
                return key.insert(0, getFullKeyOfLine(i, lines)).toString()
            }
        }
        return key.toString()
    }

    private fun parseFileComments(fileLines: List<String>): List<Comment> {
        var lastStoredIndex = -1
        val comments = ArrayList<Comment>(fileLines.size)

        for(index in fileLines.indices) {
            if(lastStoredIndex >= index) continue
            val line = fileLines[index]
            var commentMatcher = COMMENT_PATTERN.matcher(line)

            // if entire line is comment
            if (line.isBlank() || commentMatcher.matches()) {
                val commentArray = ArrayList<String>()
                var currentLine = line
                lastStoredIndex = index - 1

                while(currentLine.isBlank() || commentMatcher.matches()) {
                    lastStoredIndex++
                    commentArray.add(currentLine)
                    // breaks if we are on the last line already
                    if(fileLines.lastIndex < (lastStoredIndex + 1)) break
                    // prepare the check on the next line
                    currentLine = fileLines[lastStoredIndex + 1]
                    commentMatcher = COMMENT_PATTERN.matcher(currentLine)
                }

                val commentType = if(commentArray.size > 1) CommentType.FULL_MULTILINE else CommentType.FULL_LINE

                comments.add(
                    Comment(index = index,
                        type = commentType,
                        lineAbove = getFullKeyOfLine(index - 1, fileLines),
                        lineBelow = getFullKeyOfLine(lastStoredIndex + 1, fileLines),
                        content = commentArray)
                )
                continue
            }

            // do nothing if there is no comment on this line
            if(!commentMatcher.find()) continue

            // if it's a dangling comment on a key or entry of a list
            val keyOrEntryMatcher = KEY_PATTERN.matchEntire(line) ?: LIST_PATTERN.matchEntire(line)
            keyOrEntryMatcher?.apply {
                comments.add(
                    Comment(index = index,
                        type = CommentType.DANGLING,
                        lineAbove = getFullKeyOfLine(index - 1, fileLines),
                        lineBelow = getFullKeyOfLine(index + 1, fileLines),
                        key = groupValues[1],
                        path = getFullKeyOfLine(index, fileLines),
                        content = listOf(commentMatcher.group()))
                )
            }
        }
        return comments
    }

    private fun addCommentsToFile(comments: List<Comment>): List<String> {
        // read the new file, removing all comments (from header)
        val newFile = file.getLines().filter { !COMMENT_PATTERN.matcher(it).matches() } as MutableList<String>

        val fullLineComments = comments.filter { it.type == CommentType.FULL_MULTILINE || it.type == CommentType.FULL_LINE }
        for(comment in fullLineComments) {
            var placed = false
            while(newFile.size < comment.index) newFile.add("")

            if(comment.lineBelow.isNotBlank()) {
                for(index in 0 until newFile.size) {
                    if(getFullKeyOfLine(index, newFile) != comment.lineBelow) continue
                    newFile.addAll(max(0,  index), comment.content)
                    placed = true
                    break
                }
            }
            if(placed) continue

            if(comment.lineAbove.isNotBlank()) {
                for(index in 0 until newFile.size) {
                    if(getFullKeyOfLine(index, newFile) != comment.lineAbove) continue
                    newFile.addAll(max(0, index + 1), comment.content)
                    placed = true
                    break
                }
            }
            if(placed) continue

            newFile.addAll(comment.index, comment.content)
        }

        val danglingComments = comments.filter { it.type == CommentType.DANGLING }
        danglingComments.forEach { comment ->
            val key = comment.key
            val path = comment.path

            for(lineIndex in 0 until newFile.size) {
                val line = newFile[lineIndex]
                val element = KEY_PATTERN.matchEntire(line)?.groupValues?.getOrNull(1) ?: LIST_PATTERN.matchEntire(line)?.groupValues?.getOrNull(1)
                if (element == key && getFullKeyOfLine(lineIndex, newFile) == path) {
                    newFile[lineIndex] += comment.content[0]
                }
            }
        }
        return newFile
    }

    private fun lineDepth(line: String): Int = DEPTH_PATTERN.find(line)?.groupValues?.getOrNull(1)?.length?.div(2) ?: 0

    private fun Regex.matchOrNull(line: String, index: Int): String? = this.matchEntire(line)?.groupValues?.get(index)

    private fun List<String>.joinToArray(): ByteArray {
        val builder = StringBuilder()
        for (s in this) {
            builder.append(s).append('\n')
        }
        return builder.toString().toByteArray(charset = CHARSET)
    }

    private fun Path.getLines(): List<String> = readLines(CHARSET).map { line -> line.replace("\t", "  ") }

    private fun Path.writeLines(fileLines: List<String>){ runCatching { writeBytes(fileLines.joinToArray()) } }

    private fun newYamlLoader() = YamlConfigurationLoader.builder().indent(2).nodeStyle(NodeStyle.BLOCK).path(file).defaultOptions { it.shouldCopyDefaults(options.copyDefault) }.build()

    private companion object {
        val CHARSET = Charsets.UTF_8
        val COMMENT_PATTERN = """(\s*?#.*)$""".toPattern()
        val KEY_PATTERN = """(?i)^\s*([\w\d\-!@#$%^&*+]+?):.*$""".toRegex()
        val LIST_PATTERN = """(?i)^\s*(-\s?"?[\w\d]+"?).*$""".toRegex()
        val DEPTH_PATTERN = """^(\s+)[^\s]+""".toRegex()
    }

    /**
     * Represents a block of comments inside the file, it can be one line, multiline, or
     * dangling comments
     *
     * @property type CommentType if it's a single line, multiline, or dangling comment
     * @property index Int the line index that the comment bloc was in (0 index)
     * @property lineAbove String full key from the line above it
     * @property lineBelow String full key from the line below it
     * @property key String name of the key of the line the commentary was in (only used in case of dangling comments)
     * @property path String full key of the line the commentary was in (only used in case of dangling comments)
     * @property content List<String> lines that compose this block of comments
     * @constructor
     */
    private data class Comment (
        val type: CommentType,
        val index: Int,
        val lineAbove: String,
        val lineBelow: String,
        val key: String = "",
        val path: String = "",
        val content: List<String>
    ) {
        init {
            if(type == CommentType.FULL_MULTILINE) require(content.size > 1) { "$type comment has a content size of ${content.size}, this should not happen" }
            else require(content.size == 1) { "$type comment has a content size of ${content.size}, this should not happen" }
        }
    }

    private enum class CommentType {
        FULL_MULTILINE,
        FULL_LINE,
        DANGLING
    }
}
