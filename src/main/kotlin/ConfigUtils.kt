package top.ntutn

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class ConfigUtils {
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
        fun getConfigPathAndFilename()= getAbsolutePath(getCurrentPath(),"./config.xml")

        /**
         * 获取资源文件所在的位置
         */
        fun getResourcePath()=ConfigUtils::class.java.classLoader.getResource("")

        /**
         * 将资源文件复制到指定位置
         * @param resource 资源文件，“/”开头
         * @param to 目标位置，绝对路径，包括文件名
         */
        fun copyResourceTo(resource:String,to:String){
            val ins=this::class.java.getResourceAsStream(resource)
            val dist= Paths.get(to)
            Files.copy(ins,dist)
        }

        /**
         * 检查配置文件和主要文件夹是否存在，不存在则创建
         */
        fun checkInit() {
            if(!File(getConfigPathAndFilename()).exists()){
                println("主配置文件不存在！")
                println(getCurrentPath())
                copyResourceTo("/config.xml", getConfigPathAndFilename())
            }
        }
    }
}