package top.ntutn

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MakeTasks {
    companion object {
        val copyTask = CopyTask()
        val htmlTask = HtmlTask()
        val mainPageTask = MainPageTask()
        val noOperationTask = NoOperationTask()
    }
}

/**
 * 构建任务
 */
interface MakeTask {
    fun invoke(source: List<File>, target: File, properties: Map<String, Any>)

    fun invoke(source: List<File>, target: File) {
        invoke(source, target, emptyMap())
    }
}

/**
 * 检查文件是否可读
 */
private fun checkReadable(file: File): Boolean {
    if (!file.isFile || !file.canRead()) {
        return true
    }
    return true
}

/**
 * 不做任何事情
 */
class NoOperationTask : MakeTask {
    override fun invoke(source: List<File>, target: File, properties: Map<String, Any>) = Unit
}

/**
 * 复制任务，简单将第一个依赖文件复制为目标文件
 */
class CopyTask : MakeTask {
    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    override fun invoke(source: List<File>, target: File, properties: Map<String, Any>) {
        if (source.isEmpty() || !checkReadable(source[0])) {
            return
        }
        logger.debug("复制" + source[0] + "到" + target)
        val parentFile = target.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        FileUtils.copyFile(source[0], target)
    }
}

fun createEmptyFile(file: File) {
    if (!file.exists()) {
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()
    }
}

/**
 * 用所给md和模板渲染出一个内容网页
 * 源文件传入第一个参数为md文件，第二个参数为模板
 */
class HtmlTask : MakeTask {
    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    override fun invoke(source: List<File>, target: File, properties: Map<String, Any>) {
        if (source.size < 2 || !checkReadable(source[0]) || !checkReadable(source[1])) {
            logger.error("HTML渲染参数错误。")
            return
        }
        val mdFile = source[0]
        val modelFile = source[1]
        val modelName = modelFile.nameWithoutExtension
        val parser = MdWithConfigParser(mdFile = mdFile, renderSummary = false, renderContent = true)

        val outputHtml = HTMLTemplateUtil.render(modelName, parser.attributes.plus(ConfigUtil.siteAttributes))
        parser.editDate = Date()
        logger.info("渲染$mdFile->$target")
        createEmptyFile(target)
        target.writeText(outputHtml)
    }
}

/**
 * 用所给输入文章内容页构建一个主页
 */
class MainPageTask : MakeTask {
    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    /**
     * @param properties properties应该传递当前页码pageNum，页面总数pageCount，当前页面条目数量itemCount，单页面条目数量限制itemCountLimit
     */
    override fun invoke(source: List<File>, target: File, properties: Map<String, Any>) {
        val modelFile = source.last {
            it.canonicalPath.contains(ConfigUtil.templatePath)
        }
        val mdList = source - modelFile

        var htmls = emptyArray<Map<String, String>>()
        mdList.forEach {
            if (it.name == MakeDependency.TARGET_ALWAYS_MAKE || it.name == MakeDependency.TARGET_NO_MAKE) {
                return@forEach
            }
            htmls += MdWithConfigParser(it, renderContent = false, renderSummary = true)
                .attributes
                .plus(
                    "url" to
                            it.toRelativeString(File(ConfigUtil.inputPath))
                                .removeSuffix(".md").plus(".html")
                )
        }

        htmls.sortByDescending {
            it["editTime"]
        }

        val attributes = hashMapOf<String, Any>()

        attributes += "htmls" to htmls.map {
            it.plus("editTime2822" to convertTimeTo2822(it["editTime"] ?:"1970-01-01"))
        }
        attributes += ConfigUtil.siteAttributes
        attributes += properties

        val outputHtml = HTMLTemplateUtil.render(modelFile.nameWithoutExtension, attributes)
        logger.info("使用$modelFile 渲染$target")
        createEmptyFile(target)
        target.writeText(outputHtml)
    }

    private fun convertTimeTo2822(time:String):String{
        val fmt1: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val fmt2 = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US)
        val date=fmt1.parse(time)
        return fmt2.format(date)
    }
}