package top.ntutn

import org.dom4j.Document
import org.dom4j.io.SAXReader
import java.io.File
import java.io.FileInputStream

class XMLUtil {
    companion object{
        fun readXMLDocument(filePath:String):Document{
            val reader=SAXReader()
            val ins=FileInputStream(File(filePath))
            return reader.read(ins)
        }
    }
}