package top.ntutn

import org.apache.commons.io.FileUtils
import java.io.File

class MakeTasks {
    companion object {
        val copyTask = CopyTask()
        val htmlTask = HtmlTask()
        val mainPageTask=MainPageTask()
    }
}

/**
 * 构建任务
 */
interface MakeTask {
    fun invoke(source: List<File>, target: File)
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
 * 复制任务，简单将第一个依赖文件复制为目标文件
 */
class CopyTask : MakeTask {
    override fun invoke(source: List<File>, target: File) {
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

/**
 * 用所给md和模板渲染出一个内容网页
 * 源文件传入第一个参数为md文件，第二个参数为xm，第三个参数为模板
 */
class HtmlTask : MakeTask {

    override fun invoke(source: List<File>, target: File) {
        if (source.size < 3 || !checkReadable(source[0]) || !checkReadable(source[1]) || !checkReadable(source[2])) {
            return
        }
        val mdFile = source[0]
        val xmlFile = source[1]
        val modelFile = source[2]
        val modelName = modelFile.nameWithoutExtension

        val md = FileUtils.fileRead(mdFile.canonicalPath)
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
        FileUtils.fileWrite(target.canonicalPath, outputHtml)
    }
}

/**
 * 用所给输入文章内容页构建一个主页
 */
class MainPageTask : MakeTask {
    override fun invoke(source: List<File>, target: File) {
        val modelFile = source.last()
        val mdXmlList = source - modelFile

        var htmls = emptyArray<MutableMap<String, String>>()
        mdXmlList.forEach {
            val document = XMLUtil.readXMLDocument(it.canonicalPath)
            if (document != null) {
                val map = emptyMap<String, String>().toMutableMap()
                document.rootElement.element("attributes").elements().forEach {
                    map += it.attribute("ID").stringValue to it.textTrim
                }
                map += "url" to getRelativeOutputFileOfMd(it)
                htmls += map
            }
        }

        htmls.sortBy {
            it["editTime"]
        }

        val attributes= hashMapOf<String,Any>()

        attributes+="htmls" to htmls
        attributes+=ConfigUtil.siteAttributes

        val outputHtml=HTMLTemplateUtil.render(modelFile.nameWithoutExtension, attributes)
        println("渲染主页$target")
        FileUtils.fileWrite(target.canonicalPath, outputHtml)
    }

    fun getRelativeOutputFileOfMd(mdFile: File) =
        mdFile.relativeTo(File(ConfigUtil.inputPath)).toString().replace(".md.xml", ".html")

    //TODO 加入多文件生成功能，因为主页不一定只有一页
}