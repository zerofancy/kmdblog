package top.ntutn

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * 直接读写md文件开头的配置
 * 可对属性值直接赋值，但只有saveBack时它们才会被写入文件
 * 只有refresh时它们才会出现在hashmap中
 */
class MdWithConfigParser(val mdFile: File, renderSummary: Boolean = true, renderContent: Boolean = false) {
    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    private lateinit var _md: String
    val md
        get() = _md

    private lateinit var _html: String
    val html
        get() = _html

    lateinit var title: String
    lateinit var author: String
    lateinit var tags: List<String>

    private lateinit var _summary: String

    // summary无法从这里编辑，只能编辑markdown源文件
    val summary
        get() = _summary

    lateinit var publishDate: Date
    lateinit var editDate: Date

    private lateinit var _attributes: Map<String, String>
    val attributes
        get() = _attributes

    init {
        readConfig(renderSummary, renderContent)
    }

    private fun readConfig(renderSummary: Boolean = true, renderContent: Boolean = false) {
        _md = FileUtils.fileRead(mdFile.canonicalPath)

        val summaryMd = regSummary.find(_md)?.groupValues?.getOrNull(1) ?: ""
        _summary = if (renderSummary) {
            MdToHTMLUtil.render(summaryMd)
        } else {
            summaryMd
        }
        _html = if (renderContent) {
            MdToHTMLUtil.render(_md)
        } else {
            md
        }

        regConfig.find(_md)?.groupValues?.getOrNull(1)?.lines()?.map {
            val array = it.split(':')
            array.getOrElse(0) { "" }.trim() to array.getOrElse(1) { "" }.trim()
        }?.toMap()?.let { map ->
            _attributes = map.plus("md" to _md).plus("summary" to _summary)
            title = map["title"] ?: ""
            author = map["author"] ?: ConfigUtil.siteAttributes["defaultAuthor"] ?: ""
            publishDate = try {
                dateFormat.parse(map["publishDate"])
            } catch (e: Exception) {
                Date()
            }
            editDate = try {
                dateFormat.parse(map["editDate"])
            } catch (e: Exception) {
                Date()
            }
            tags = map["tags"]?.removePrefix("[")?.removeSuffix("]")?.split(',')?.map { it.trim() } ?: listOf()
        }
        if (!this::_attributes.isInitialized) {
            _attributes = mapOf()
            title = ""
            author = ConfigUtil.siteAttributes["defaultAuthor"] ?: ""
            publishDate = Date()
            editDate = Date()
            tags = listOf()
        }
        _attributes = _attributes.plus("md" to _md).plus("html" to _html)
    }

    fun refresh(renderSummary: Boolean = true, renderContent: Boolean = false) {
        readConfig(renderSummary, renderContent)
    }

    fun saveBack() {
        var content = FileUtils.fileRead(mdFile.canonicalPath)
        if (content.isBlank()) {
            content = "<!--more-->"
        }
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

        var newContent = content.replace(regConfig, stringConfig)
        // 若原来md文件没有配置信息，就把配置信息放到开头
        newContent = if (newContent == content) "$stringConfig\n$content" else newContent

        FileUtils.fileWrite(mdFile.canonicalPath, newContent)
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