package top.ntutn

import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.SimTocExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet

class MdToHTMLUtil {
    companion object {
        private val options = MutableDataSet().set(
            Parser.EXTENSIONS,
            listOf(
                TablesExtension.create(),
                TocExtension.create()
            )
        )
            .set(SimTocExtension.LEVELS, 255)
            .set(TocExtension.TITLE, "Table of Contents")
            .set(TocExtension.DIV_CLASS, "toc")
            .set(TocExtension.TITLE_LEVEL,2)
        private val parser = Parser.builder(options).build()
        private val renderer = HtmlRenderer.builder(options).build()

        fun render(md: String): String {
            return renderer.render(parser.parse(md))
        }
    }
}