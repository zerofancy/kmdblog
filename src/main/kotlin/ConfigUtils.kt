package top.ntutn

import java.util.*

class ConfigUtils {
    companion object {
        fun readValue(filePath: String, key: String): String {
            val props=Properties()
            println(ConfigUtils::class.java.classLoader.getResource("config.xml"))
            return ""
        }
    }
}