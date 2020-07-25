package top.ntutn

import java.io.File

/**
 * 用于存储文件之间的依赖关系的数据结构
 * @param sourceFiles 此构建关系需要的源文件。如果想要某个目标保持构建，那么可以依赖一个名为“TARGET_ALWAYS_REMAKE”
 * @param targetFile 此构建关系应该产生的目标文件
 * @param makeTask 执行构建的函数
 */
class MakeDependency(val sourceFiles: List<File>, val targetFile: File, var makeTask:MakeTask) {
    companion object{
        val TARGET_NO_MAKE="TARGET_NO_MAKE"
        val TARGET_ALWAYS_MAKE="TARGET_ALWAYS_MAKE"
    }

    /**
     * 检查是否需要重新构建这个文件。依据：源文件有任何一个修改日期晚于目标文件修改日期，则应执行构建。
     * @return 是否需要重新构建
     */
    fun shouldMakeAgain(): Boolean {
        sourceFiles.forEach {
            if(it.name == TARGET_NO_MAKE){
                return false
            }
            if (it.name == TARGET_ALWAYS_MAKE || it.lastModified() > targetFile.lastModified()) {
                return true
            }
        }
        return false
    }

    /**
     * 执行构建。先删除目标文件，再执行构建
     * @return 构建是否成功了
     */
    fun makeAgain(): Boolean {
        if (targetFile.exists()) {
            if (!targetFile.delete()) {
                return false
            }
        }
        makeTask.invoke(sourceFiles,targetFile)
        return targetFile.exists()
    }
}
