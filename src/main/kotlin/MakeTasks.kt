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

        val outputHtml = HTMLTemplateUtil.render(modelName, hashMapOf("md" to md, "html" to mdHtml))
        println("渲染$mdFile->$target")
        FileUtils.fileWrite(target.canonicalPath, outputHtml)
    }
}