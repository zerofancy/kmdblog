package top.ntutn

import org.apache.commons.io.FileUtils
import java.io.File

class MakeTasks {
    companion object {
        val copyTask = CopyTask()
    }
}

interface MakeTask {
    /**
     *
     */
    fun invoke(source: List<File>, target: File)
}

class CopyTask : MakeTask {
    override fun invoke(source: List<File>, target: File) {
        if (source.isEmpty() || !source[0].isFile || !source[0].canRead()) {
            return
        }
        print("复制" + source[0] + "到" + target)
        val parentFile = target.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        FileUtils.copyFile(source[0], target)
    }
}