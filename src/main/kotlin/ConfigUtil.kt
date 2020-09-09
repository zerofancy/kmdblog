package top.ntutn

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * 与系统的通用配置项有关的类。由于配置项是在启动时初始化，目前checkInit()也被放到了这里。
 */
class ConfigUtil {
    companion object {
        /**
         * 获取当前路径
         */
        fun getCurrentPath() = System.getProperty("user.dir");

        /**
         * 获取绝对路径
         * @param currentPath 父文件夹路径
         * @param relative 相对路径
         */
        fun getAbsolutePath(currentPath: String, relative: String) = File(currentPath, relative).canonicalPath

        /**
         * 获取主配置文件所在的路径（包括文件名）
         */
        fun getConfigPathAndFilename() = getAbsolutePath(getCurrentPath(), "./config.xml")

        /**
         * 获取资源文件所在的位置
         */
        fun getResourcePath() = ConfigUtil::class.java.classLoader.getResource("")

        var inputPath: String
            private set
        var outputPath: String
            private set
        var staticPath: String
            private set
        var templatePath: String
            private set
        var siteAttributes: HashMap<String, String>
            private set
        var noCleanFiles: Array<File>
            private set

        /**
         * 将资源文件复制到指定位置
         * @param resource 资源文件，“/”开头
         * @param to 目标位置，绝对路径，包括文件名
         */
        fun copyResourceTo(resource: String, to: String) {
            try {
                val ins = this::class.java.getResourceAsStream(resource)
                val dist = Paths.get(to)
                Files.copy(ins, dist)
            } catch (e: FileAlreadyExistsException) {
                println("文件${to}已经存在了。")
            }
        }

        /**
         * 检查配置文件和主要文件夹是否存在，不存在则创建
         */
        init {
            if (!File(getConfigPathAndFilename()).exists()) {
                println("主配置文件不存在！")
                println(getCurrentPath())
                try {
                    copyResourceTo("/config.xml", getConfigPathAndFilename())
                } catch (e: Exception) {
                    println("复制文件出错。")
                    exitProcess(1)
                }
            }
            val document = XMLUtil.readXMLDocument(getConfigPathAndFilename())
            if (document == null) {
                println("XML读取失败。")
                exitProcess(1)
            }

            //读取所有的站点配置
            siteAttributes = HashMap<String, String>()

            //读取不会被删除的文件配置
            noCleanFiles = emptyArray<File>()
            document.rootElement.element("noclean").elements().forEach {
                noCleanFiles += File(it.textTrim)
            }

            document.rootElement.element("attributes").elements().forEach {
                siteAttributes[it.attributeValue("ID")] = it.textTrim
            }

            with(document.rootElement.element("path")) {
                inputPath = getAbsolutePath(getCurrentPath(), attributeValue("input"))
                outputPath = getAbsolutePath(getCurrentPath(), attributeValue("output"))
                staticPath = getAbsolutePath(getCurrentPath(), attributeValue("static"))
                templatePath = getAbsolutePath(getCurrentPath(), attributeValue("templates"))
            }
            println("读取到关键路径：input=$inputPath，output=$outputPath，static=$staticPath，template=$templatePath")

            val inputDir = File(inputPath)
            val outputDir = File(outputPath)
            val staticDir = File(staticPath)
            val staticResDir = File(staticPath, "./res")
            val templateDir = File(templatePath)

            if (!inputDir.exists()) {
                println("input文件夹不存在，正在创建。")
                inputDir.mkdirs()
            }
            if (!outputDir.exists()) {
                println("output文件夹不存在，正在创建。")
                outputDir.mkdirs()
            }
            if (!templateDir.exists()) {
                println("模板文件夹不存在，正在创建。")
                templateDir.mkdirs()
            }
            if (!staticDir.exists()) {
                println("静态资源文件夹不存在，正在创建。")
                staticDir.mkdirs()
            }
            if (!staticResDir.exists()) {
                println("静态设计资源文件夹不存在，正在创建。")
                staticResDir.mkdirs()
            }

            val indexHtmlPath = getAbsolutePath(templatePath, "./index.html")
            if (!File(indexHtmlPath).exists()) {
                println("主页模板文件不存在！")
                copyResourceTo("/templates/index.html", indexHtmlPath)
            }

            val articleHtmlPath = getAbsolutePath(templatePath, "./article.html")
            if (!File(articleHtmlPath).exists()) {
                println("文章模板文件不存在！")
                copyResourceTo("/templates/article.html", articleHtmlPath)
            }

            val rssHtmlPath = getAbsolutePath(templatePath, "./rss.html")
            if (!File(rssHtmlPath).exists()) {
                println("RSS模板文件不存在！")
                copyResourceTo("/templates/rss.html", rssHtmlPath)
            }

            val faviconPath = getAbsolutePath(staticPath, "./favicon.ico")
            if (!File(faviconPath).exists()) {
                println("网站图标不存在！")
                copyResourceTo("/static/favicon.ico", faviconPath)
            }

            val aboutMdPath = getAbsolutePath(inputPath, "./about.md")
            if (!File(aboutMdPath).exists()) {
                println("网站关于模板不存在！")
                copyResourceTo("/input/about.md", aboutMdPath)
            }

            val logoPath = getAbsolutePath(staticPath, "./res/logo.png")
            if (!File(logoPath).exists()) {
                println("网站logo模板不存在！")
                copyResourceTo("/static/res/logo.png", logoPath)
            }
        }
    }
}