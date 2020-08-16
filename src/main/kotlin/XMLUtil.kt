package top.ntutn

import org.dom4j.Branch
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

/**
 * XML工具类
 */
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

        fun writeXMLDocument(document: Document,file: File){
            if(file.exists()){
                file.delete()
            }
            val format: OutputFormat = OutputFormat.createPrettyPrint()
            format.encoding = document.xmlEncoding
            val xmlWriter= XMLWriter(FileWriter(file),format)
            xmlWriter.write(document)
            xmlWriter.close()
        }

        fun createElement(parent: Branch, name:String, attrs: HashMap<String,String>, text:String):Element{
            val element= parent.addElement(name)
            attrs.forEach {
                element.addAttribute(it.key,it.value)
            }
            element.text=text
            return element
        }
    }
}