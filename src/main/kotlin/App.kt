package top.ntutn

import org.apache.commons.io.FileUtils
import org.dom4j.Document
import org.dom4j.DocumentFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    println("kmdblog已经启动！")

    HTMLTemplateUtil.initEngine(ConfigUtil.templatePath)

    val dependencyList = LinkedList<MakeDependency>()

    //遍历静态文件夹，添加依赖
    scanStaticFolder(File(ConfigUtil.staticPath), dependencyList, ConfigUtil.outputPath)
    //res还需要复制到其他文件夹
    scanStaticFolder(File(ConfigUtil.staticPath + "/res"), dependencyList, ConfigUtil.inputPath)
    scanStaticFolder(File(ConfigUtil.staticPath + "/res"), dependencyList, ConfigUtil.templatePath)

    scanMdFolder(File(ConfigUtil.inputPath), dependencyList)

    /*
    TODO 生成内容页

    TODO markdown渲染
    需要一个java/kotlin实现的markdown渲染引擎。

    TODO 生成首页
     */

    dependencyList.forEach {
        if (it.shouldMakeAgain()) {
            it.makeAgain()
        }
    }

    // 删除多余文件和空文件夹
    removeUnusedFiles(File(ConfigUtil.outputPath), dependencyList)
    removeUnusedFiles(File(ConfigUtil.inputPath, "./res"), dependencyList)
    removeUnusedFiles(File(ConfigUtil.templatePath, "./res"), dependencyList)
    removeEmptyFolders(File(ConfigUtil.outputPath))
    removeEmptyFolders(File(ConfigUtil.inputPath, "./res"))
    removeEmptyFolders(File(ConfigUtil.outputPath, "./res"))
}

/**
 * 扫描输入文件夹，同步XML数据并开始渲染
 * @param root 起始位置
 * @param arrayDependency 已有依赖列表
 */
fun scanMdFolder(root: File, arrayDependency: LinkedList<MakeDependency>) {
    if (!root.exists() || !root.isDirectory || !root.canRead()) {
        return
    }
    //跳过res文件夹
    if (File(ConfigUtil.inputPath + "/res").exists() && Files.isSameFile(
            Paths.get(ConfigUtil.inputPath + "/res"),
            Paths.get(root.toURI())
        )
    ) {
        return
    }
    root.listFiles()?.forEach {
        if (it.isDirectory) {
            scanMdFolder(it, arrayDependency)
            return@forEach
        }
        if (it.name.endsWith(".md")) {
            val mdXml = File(it.parent, it.name + ".xml")
            val model = File(ConfigUtil.templatePath, "article.html")
            val target = File(ConfigUtil.outputPath, it.nameWithoutExtension + ".html")
            updateMdXml(it, mdXml)
            arrayDependency.add(MakeDependency(listOf(it, mdXml, model), target, MakeTasks.htmlTask))
        }
    }
}

fun updateMdXml(md: File, mdXml: File) {
    var mdXmlDocument: Document? = XMLUtil.readXMLDocument(mdXml.toString())
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    if (mdXmlDocument == null) {
        println("未找到${md}的配置文件")
        mdXmlDocument = DocumentFactory.getInstance().createDocument()
        val attrElement = mdXmlDocument.addElement("article").addElement("attributes")
        XMLUtil.createElement(attrElement, "attr", hashMapOf("ID" to "title"), md.name.replace(".md", ""))
        XMLUtil.createElement(attrElement, "attr", hashMapOf("ID" to "keywords"), "文章|关键词")
        XMLUtil.createElement(attrElement, "attr", hashMapOf("ID" to "abs"), "文章的简介")
        XMLUtil.createElement(attrElement, "attr", hashMapOf("ID" to "publishTime"), simpleDateFormat.format(Date()))
        XMLUtil.createElement(attrElement, "attr", hashMapOf("ID" to "editTime"), simpleDateFormat.format(Date()))
        XMLUtil.createElement(
            attrElement,
            "attr",
            hashMapOf("ID" to "author"),
            ConfigUtil.siteAttributes["defaultAuthor"] ?: "NO NAME"
        )
    }
    mdXmlDocument!!
    //更新修改日期
    mdXmlDocument.elementByID("editTime").text = simpleDateFormat.format(md.lastModified())
    XMLUtil.writeXMLDocument(mdXmlDocument, mdXml)
}

fun scanStaticFolder(root: File, arrayDependency: LinkedList<MakeDependency>, targetPath: String) {
    if (!root.exists() || !root.isDirectory || !root.canRead()) {
        return
    }
    root.listFiles()?.forEach {
        if (it.isDirectory) {
            scanStaticFolder(it, arrayDependency, targetPath)
            return@forEach
        }
        arrayDependency.add(
            MakeDependency(
                listOf(it),
                File(targetPath, it.relativeToOrSelf(File(ConfigUtil.staticPath)).toString()),
                MakeTasks.copyTask
            )
        )

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