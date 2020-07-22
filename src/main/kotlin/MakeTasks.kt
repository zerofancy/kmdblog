package top.ntutn

import org.apache.commons.io.FileUtils
import java.io.File

class MakeTasks {
    companion object {
        val copyTask = CopyTask()
        val htmlTask = HtmlTask()
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

        val attributes= hashMapOf<String,Any>()

        val document=XMLUtil.readXMLDocument(xmlFile.canonicalPath)
        document?.rootElement?.element("attributes")?.elements()?.forEach {
            attributes+=it.attribute("ID").stringValue to it.textTrim
        }
        attributes+="md" to md
        attributes+="html" to mdHtml
        attributes+=ConfigUtil.siteAttributes

        val outputHtml = HTMLTemplateUtil.render(modelName, attributes)
        println("渲染$mdFile->$target")
        FileUtils.fileWrite(target.canonicalPath, outputHtml)
    }
}

/**
 * 用所给输入文章内容页构建一个主页
 * TODO 决定到底是用md文件输入还是用html文件输入。
 * 或许无输入更好（只有模板输入），始终构建，主动扫描
 */
class MainPageTask:MakeTask{
    override fun invoke(source: List<File>, target: File) {
        /**
         * 扫描所有mdxml文件，得到站点结构和所有文章的属性{数据结构？}
         * 按照修改时间对输出文件排序
         * 附加站点属性，模板渲染
         *
         * 那么传递给模板的数据有站点属性String to String，文章属性String to HashMap
         */
    }

}