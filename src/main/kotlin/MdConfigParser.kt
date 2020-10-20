package top.ntutn

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * 直接读写md文件开头的配置
 */
class MdConfigParser(val mdFile: File) {
    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    lateinit var title: String
    lateinit var author: String
    lateinit var tags: List<String>
    private lateinit var _summary: String

    // summary无法从这里编辑，只能编辑markdown源文件
    val summary
        get() = _summary
    lateinit var publishDate: Date
    lateinit var editDate: Date

    private lateinit var attributes: Map<String, String>

    init {
        readConfig()
    }

    private fun readConfig() {
        val content = FileUtils.fileRead(mdFile.canonicalPath)

        val summaryMd = regSummary.find(content)?.groupValues?.getOrNull(1) ?: ""
        _summary = MdToHTMLUtil.render(summaryMd)

        regConfig.find(content)?.groupValues?.getOrNull(1)?.lines()?.map {
            val array = it.split(':')
            array.getOrElse(0) { "" }.trim() to array.getOrElse(1) { "" }.trim()
        }?.toMap()?.let {
            attributes = it
            title = it["title"] ?: ""
            author = it["author"] ?: ConfigUtil.siteAttributes["defaultAuthor"] ?: ""
            publishDate = try {
                dateFormat.parse(it["publishDate"])
            } catch (e: Exception) {
                Date()
            }
            editDate = try {
                dateFormat.parse(it["editDate"])
            } catch (e: Exception) {
                Date()
            }
            tags = it["tags"]?.removePrefix("[")?.removeSuffix("]")?.split(',')?.map { it.trim() } ?: listOf()
        }
        if (!this::attributes.isInitialized) {
            attributes = mapOf()
            title = ""
            author = ConfigUtil.siteAttributes["defaultAuthor"] ?: ""
            publishDate = Date()
            editDate = Date()
            tags = listOf()
        }
    }

    fun refresh() {
        readConfig()
    }

    fun saveBack() {
        var content = FileUtils.fileRead(mdFile.canonicalPath)
        var stringConfig = "---\n"
        mapOf(
            "title" to title,
            "author" to author,
            "publishDate" to dateFormat.format(publishDate),
            "editDate" to dateFormat.format(editDate),
            "tags" to tags
        ).map {
            it.component1() + ": " + it.component2() + "\n"
        }.forEach { stringConfig += it }
        stringConfig += "---\n-"

        content = content.replace(regConfig, stringConfig)

        FileUtils.fileWrite(mdFile.canonicalPath, content)
    }

    companion object {
        val regSummary = Regex(
            """
            ([\s\S]*?)<!--.*?more.*?-->
        """.trimIndent()
        )
        val regConfig = Regex(
            """
            ---([\s\S]*?)---\s*?-
        """.trimIndent()
        )
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    }
}