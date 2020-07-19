package top.ntutn

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

fun main() {
    println("kmdblog已经启动！")

    val dependencyList = LinkedList<MakeDependency>()

    //遍历静态文件夹，添加依赖
    scanStaticFolder(File(ConfigUtil.staticPath), dependencyList)

    //遍历md文件，生成xml文件

    /*
    TODO markdown渲染
    需要一个java/kotlin实现的markdown渲染引擎。

    TODO 模板渲染
    采用thymeleaf作为模板引擎，传入的变量包括站点范围的变量、markdown引擎输出的KV对、文章信息。

     */

    dependencyList.forEach {
        if (it.shouldMakeAgain()) {
            it.makeAgain()
        }
    }

    // 删除多余文件和空文件夹
    removeUnusedFiles(File(ConfigUtil.outputPath), dependencyList)
    removeEmptyFolders(File(ConfigUtil.outputPath))
}

fun scanStaticFolder(root: File, arrayDependency: LinkedList<MakeDependency>) {
    if (!root.exists() || !root.isDirectory || !root.canRead()) {
        return
    }
    root.listFiles()?.forEach {
        if (it.isDirectory) {
            scanStaticFolder(it, arrayDependency)
        } else if (it.isFile) {
            arrayDependency.add(
                MakeDependency(
                    listOf(it),
                    File(ConfigUtil.outputPath, it.relativeToOrSelf(File(ConfigUtil.staticPath)).toString()),
                    MakeTasks.copyTask
                )
            )
        }
    }
}

/**
 * 删除“依赖关系不满足”的文件
 * @param root 遍历起始位置
 * @param outputArray 理论上的输出文件
 */
fun removeUnusedFiles(root: File, outputArray: List<MakeDependency>) {
    if (root.isDirectory) {
        root.listFiles()?.forEach { reality ->
            removeUnusedFiles(reality, outputArray)
            if (reality.isFile) {
                var used = false
                outputArray.forEach the@{ theory ->
                    if (Files.isSameFile(Paths.get(theory.targetFile.toURI()), Paths.get(reality.toURI()))) {
                        used = true
                        return@the
                    }
                }
                if (!used) {
                    println("删除无用文件$reality")
                    reality.delete()
                }
            }
        }
    }
}

fun removeEmptyFolders(file: File) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { removeEmptyFolders(it) }
        if (file.listFiles()?.isEmpty() != false) {
            println("删除空文件夹$file")
            file.delete()
        }
    }
}