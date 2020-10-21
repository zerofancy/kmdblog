package top.ntutn

import BuildConfig
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.dom4j.Document
import org.dom4j.DocumentFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

private val logger by lazy { LoggerFactory.getLogger(Unit::class.java) }

class MyArgs(parser: ArgParser) {
    val newName by parser.storing("-n", "--new", help = "新建一篇文章")
        .default<String?>(null)
    val generate by parser.flagging("-g", "--generate", help = "生成静态网页")
    val version by parser.flagging("-v", "--version", help = "查看当前版本")
}

fun main(args: Array<String>) {
    logger.trace("kmdblog启动。")
    mainBody {
        ArgParser(args).parseInto(::MyArgs).run {
            newName?.let {
                createNewBlog(it)
                return@mainBody
            }
            if (generate) {
                generateStatic()
                return@mainBody
            }
            if (version) {
                logger.info(BuildConfig.versionName)
                return@mainBody
            }
        }
        logger.info("未匹配任何命令，使用--help参数查看帮助。")
    }
}

fun generateStatic() {
    HTMLTemplateUtil.initEngine(ConfigUtil.templatePath)

    val dependencyList = LinkedList<MakeDependency>()

    //遍历静态文件夹，添加依赖
    scanStaticFolder(File(ConfigUtil.staticPath), dependencyList, ConfigUtil.outputPath)

    //生成文章内容页
    val mds = scanMdFolder(File(ConfigUtil.inputPath), dependencyList, arrayOf())

    //生成首页
//    val mdXmls = scanMdXmls(File(ConfigUtil.inputPath), emptyArray()).sortedByDescending {
//        val date = XMLUtil.readXMLDocument(it.canonicalPath)?.elementByID("editTime")?.textTrim
//        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//    }

    //每x个生成一个页面
    var counter = 1;
    var splitItemNum = ConfigUtil.siteAttributes["indexSplitItemNum"]?.toInt() ?: 5
    if (splitItemNum < 1) {
        splitItemNum = 5
    }
    val depList = MdToHTMLUtil.averageAssignFixLength(mds.toList(), splitItemNum)
    depList.forEach {
        val tmpList = it.toMutableList()
        val firstPageName = "index.html"
        val pageName = "index$counter.html"
        if (counter == 1) {
            logger.trace("渲染rss订阅页面。")
            val tmpList2 = tmpList.toMutableList()
            tmpList2 += File(ConfigUtil.templatePath, "rss.html")
            tmpList2 += File(MakeDependency.TARGET_ALWAYS_MAKE)
            dependencyList.add(
                MakeDependency(
                    tmpList2,
                    File(ConfigUtil.outputPath, "rss.xml"),
                    MakeTasks.mainPageTask,
                    mapOf(
                        "pageNum" to counter,
                        "pageCount" to depList.size,
                        "itemCount" to it.size,
                        "itemCountLimit" to splitItemNum
                    )
                )
            )
        }
        tmpList += File(ConfigUtil.templatePath, "index.html")
        tmpList += File(MakeDependency.TARGET_ALWAYS_MAKE)
        dependencyList.add(
            MakeDependency(
                tmpList,
                File(ConfigUtil.outputPath, if (counter == 1) firstPageName else pageName),
                MakeTasks.mainPageTask,
                mapOf(
                    "pageNum" to counter,
                    "pageCount" to depList.size,
                    "itemCount" to it.size,
                    "itemCountLimit" to splitItemNum
                )
            )
        )
        counter++
    }

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

fun createNewBlog(filename: String, author: String? = null) {
    val _author = author ?: ConfigUtil.siteAttributes["defaultAuthor"] ?: "归零幻想"
    val newFile = File(ConfigUtil.inputPath, "$filename.md")
    if (newFile.exists()) {
        logger.warn("文件${newFile}已经存在！")
        return
    }
    newFile.createNewFile()
    MdWithConfigParser(
        newFile,
        renderSummary = false, renderContent = false
    ).apply {
        title = filename
        this.author = _author
        tags = listOf("tag1", "tag2")
        publishDate = Date()
        editDate = Date()
        saveBack()
    }
}

/**
 * 扫描输入文件夹，返回所有mdXml文件，以便生成主页
 */
@Deprecated("不再使用mdXml")
fun scanMdXmls(root: File, array: Array<File>): Array<File> {
    var res = array
    if (!root.exists() || !root.isDirectory || !root.canRead()) {
        return res
    }
    //跳过res文件夹
    if (File(ConfigUtil.inputPath + "/res").exists() && Files.isSameFile(
            Paths.get(ConfigUtil.inputPath + "/res"),
            Paths.get(root.toURI())
        )
    ) {
        return res
    }
    root.listFiles()?.forEach {
        if (it.isDirectory) {
            res += scanMdXmls(it, array)
            return@forEach
        }
        if (it.name.endsWith(".md.xml")) {
            res += it
        }
    }
    return res
}

/**
 * 扫描输入文件夹，同步XML数据并开始渲染
 * @param root 起始位置
 * @param arrayDependency 已有依赖列表
 */
fun scanMdFolder(root: File, arrayDependency: LinkedList<MakeDependency>, array: Array<File>): Array<File> {
    var _array = array
    if (!root.exists() || !root.isDirectory || !root.canRead()) {
        return _array
    }
    //跳过res文件夹
    if (File(ConfigUtil.inputPath + "/res").exists() && Files.isSameFile(
            Paths.get(ConfigUtil.inputPath + "/res"),
            Paths.get(root.toURI())
        )
    ) {
        return _array
    }
    root.listFiles()?.forEach {
        if (it.isDirectory) {
            scanMdFolder(it, arrayDependency, _array)
            return@forEach
        }
        if (it.name.endsWith(".md")) {
            _array += it
//            val mdXml = File(it.parent, it.name + ".xml")
            val model = File(ConfigUtil.templatePath, "article.html")
            val target = File(
                ConfigUtil.outputPath,
                it.relativeToOrSelf(File(ConfigUtil.inputPath)).toString().removeSuffix(".md") + ".html"
            )
//            updateMdXml(it, mdXml)
            arrayDependency.add(
                MakeDependency(
                    listOf(
                        it
//                , mdXml
                        , model
                    ), target, MakeTasks.htmlTask
                )
            )
        }
    }
    return _array
}

@Deprecated("不再使用mdXml", ReplaceWith("MdWithConfigParser"))
fun updateMdXml(md: File, mdXml: File) {
    var mdXmlDocument: Document? = XMLUtil.readXMLDocument(mdXml.toString())
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    var edited = false
    if (mdXmlDocument == null) {
        edited = true
        logger.warn("未找到${md}的配置文件")
        mdXmlDocument = DocumentFactory.getInstance().createDocument()
        val attrElement = mdXmlDocument.addElement("article").addElement("attributes")
        XMLUtil.createElement(attrElement, "attr", hashMapOf("ID" to "title"), md.name.removeSuffix(".md"))
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
    if (md.lastModified() > mdXml.lastModified()) {
        edited = true
        mdXmlDocument.elementByID("editTime").text = simpleDateFormat.format(md.lastModified())
    }
    if (edited) {
        XMLUtil.writeXMLDocument(mdXmlDocument, mdXml)
    }
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
        root.listFiles()?.forEach delnouse@{ reality ->
            ConfigUtil.noCleanFiles.forEach {
                if (it.exists() && Files.isSameFile(Paths.get(reality.toURI()), Paths.get(it.toURI()))) {
                    return@delnouse
                }
            }

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
                    logger.info("删除无用文件$reality")
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
            logger.info("删除空文件夹$file")
            file.delete()
        }
    }
}