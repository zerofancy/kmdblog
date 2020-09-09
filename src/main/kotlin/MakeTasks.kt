package top.ntutn

import org.apache.commons.io.FileUtils
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
    override fun invoke(source: List<File>, target: File, properties: Map<String, Any>) {
        if (source.isEmpty() || !checkReadable(source[0])) {
            return
        }
        println("复制" + source[0] + "到" + target)
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
 * 源文件传入第一个参数为md文件，第二个参数为xm，第三个参数为模板
 */
class HtmlTask : MakeTask {

    override fun invoke(source: List<File>, target: File, properties: Map<String, Any>) {
        if (source.size < 3 || !checkReadable(source[0]) || !checkReadable(source[1]) || !checkReadable(source[2])) {
            return
        }
        val mdFile = source[0]
        val xmlFile = source[1]
        val modelFile = source[2]
        val modelName = modelFile.nameWithoutExtension

        val md = File(mdFile.canonicalPath).readText()
        val mdHtml = MdToHTMLUtil.render(md)

        val attributes = hashMapOf<String, Any>()

        val document = XMLUtil.readXMLDocument(xmlFile.canonicalPath)
        document?.rootElement?.element("attributes")?.elements()?.forEach {
            attributes += it.attribute("ID").stringValue to it.textTrim
        }
        attributes += "md" to md
        attributes += "html" to mdHtml
        attributes += ConfigUtil.siteAttributes

        val outputHtml = HTMLTemplateUtil.render(modelName, attributes)
        println("渲染$mdFile->$target")
        createEmptyFile(target)
        target.writeText(outputHtml)
    }
}

/**
 * 用所给输入文章内容页构建一个主页
 */
class MainPageTask : MakeTask {
    /**
     * @param properties properties应该传递当前页码pageNum，页面总数pageCount，当前页面条目数量itemCount，单页面条目数量限制itemCountLimit
     */
    override fun invoke(source: List<File>, target: File, properties: Map<String, Any>) {
        val modelFile = source.last {
            it.canonicalPath.contains(ConfigUtil.templatePath)
        }
        val mdXmlList = source - modelFile

        var htmls = emptyArray<MutableMap<String, String>>()
        mdXmlList.forEach {
            if (it.name.equals(MakeDependency.TARGET_ALWAYS_MAKE) || it.name.equals(MakeDependency.TARGET_NO_MAKE)) {
                return@forEach
            }
            val document = XMLUtil.readXMLDocument(it.canonicalPath)
            if (document != null) {
                val map = emptyMap<String, String>().toMutableMap()
                document.rootElement.element("attributes").elements().forEach {
                    map += it.attribute("ID").stringValue to it.textTrim
                }
                map+="editTime2822" to convertTimeTo2822(map["editTime"]?:"1970-01-01")
                map += "url" to getRelativeOutputFileOfMd(it)
                htmls += map
            }
        }

        htmls.sortByDescending {
            it["editTime"]
        }

        val attributes = hashMapOf<String, Any>()

        attributes += "htmls" to htmls
        attributes += ConfigUtil.siteAttributes
        attributes += properties

        val outputHtml = HTMLTemplateUtil.render(modelFile.nameWithoutExtension, attributes)
        println("使用$modelFile 渲染$target")
        createEmptyFile(target)
        target.writeText(outputHtml)
    }

    private fun getRelativeOutputFileOfMd(mdFile: File) =
        mdFile.relativeTo(File(ConfigUtil.inputPath)).toString().removeSuffix(".md.xml") + ".html"

    private fun convertTimeTo2822(time:String):String{
        val fmt1: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val fmt2 = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US)
        val date=fmt1.parse(time)
        return fmt2.format(date)
    }
}