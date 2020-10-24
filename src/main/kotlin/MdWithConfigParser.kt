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

    private lateinit var configString: String
    private lateinit var summaryString: String
    private lateinit var contentWithoutSummaryString: String

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

    private fun splitArticle(source: String) {
        configString = ""
        summaryString = ""
        contentWithoutSummaryString = ""
        var isConfigAppeared = false
        var isSummaryAppeared = false
        for (line in source.lines()) {
            if (isSummaryAppeared) {
                contentWithoutSummaryString = "$contentWithoutSummaryString\n$line"
                continue
            }
            if (isConfigAppeared) {
                if (line.trim() == summarySpliterator) {
                    isSummaryAppeared = true
                    continue
                }
                summaryString = "$summaryString\n$line"
            }
            if (line.trim() == configSpliterator) {
                isConfigAppeared = true
                continue
            }
            configString = "$configString\n$line"
        }
    }

    private fun readConfig(renderSummary: Boolean = true, renderContent: Boolean = false) {
        _md = FileUtils.readFileToString(mdFile, mdCharset)
        splitArticle(_md)

        _summary = if (renderSummary) {
            MdToHTMLUtil.render(summaryString)
        } else {
            summaryString
        }
        _html = if (renderContent) {
            MdToHTMLUtil.render(_md)
        } else {
            md
        }

        configString.lines().map {
            val array = it.split(':')
            array.getOrElse(0) { "" }.trim() to array.getOrElse(1) { "" }.trim()
        }.toMap().let { map ->
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
        _attributes = _attributes.plus("md" to _md).plus("html" to _html).plus("summary" to _summary)
    }

    fun refresh(renderSummary: Boolean = true, renderContent: Boolean = false) {
        readConfig(renderSummary, renderContent)
    }

    fun saveBack() {
        var stringConfig = ""
        mapOf(
            "title" to title,
            "author" to author,
            "publishDate" to dateFormat.format(publishDate),
            "editDate" to dateFormat.format(editDate),
            "tags" to tags
        ).map {
            it.component1() + ": " + it.component2() + "\n"
        }.forEach { stringConfig += it }

        FileUtils.writeStringToFile(
            mdFile,
            "$stringConfig\n$configSpliterator\n$summaryString\n$summarySpliterator\n$contentWithoutSummaryString",
            mdCharset
        )
    }

    companion object {
        /**
         * 配置区域与summary区域的分隔符
         */
        const val configSpliterator = "<!--config-->"

        /**
         * summary区域与剩余正文的分隔符
         */
        const val summarySpliterator = "<!--summary-->"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val mdCharset = charset("UTF-8")
    }
}