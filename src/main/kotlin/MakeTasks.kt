package top.ntutn

import org.apache.commons.io.FileUtils
import java.io.File

class MakeTasks {
    companion object {
        val copyTask = CopyTask()
    }
}

/**
 * 构建任务
 */
interface MakeTask {
    fun invoke(source: List<File>, target: File)
}

/**
 * 复制任务，简单将第一个依赖文件复制为目标文件
 */
class CopyTask : MakeTask {
    override fun invoke(source: List<File>, target: File) {
        if (source.isEmpty() || !source[0].isFile || !source[0].canRead()) {
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
 * 源文件传入第一个参数为md文件，第二个参数为模板
 */
class HtmlTask:MakeTask{
    override fun invoke(source: List<File>, target: File) {
        if(source.size<2||!source[0].isFile||!source[0].canRead()||!source[1].isFile||!source[1].canRead()){
            return
        }

        TODO("Not yet implemented")
    }

}