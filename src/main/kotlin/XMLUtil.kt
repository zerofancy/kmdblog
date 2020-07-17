package top.ntutn

import org.dom4j.Document
import org.dom4j.io.SAXReader
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

class XMLUtil {
    companion object {
        fun readXMLDocument(filePath: String): Document? {
            try {
                val reader = SAXReader()
                val ins = FileInputStream(File(filePath))
                return reader.read(ins)
            } catch (e: Exception) {
                return null
            }
        }
    }
}