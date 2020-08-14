package top.ntutn

import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.SimTocExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension

class MdToHTMLUtil {
    companion object {
        private val options = MutableDataSet().set(
            Parser.EXTENSIONS,
            listOf(
                TablesExtension.create(),
                TocExtension.create(),
                StrikethroughExtension.create(),
                FootnoteExtension.create(),
                TaskListExtension.create()
            )
        )
            .set(SimTocExtension.LEVELS, 255)
            .set(TocExtension.TITLE, "Table of Contents")
            .set(TocExtension.DIV_CLASS, "toc")
            .set(TocExtension.TITLE_LEVEL, 2)
        private val parser = Parser.builder(options).build()
        private val renderer = HtmlRenderer.builder(options).build()

        fun render(md: String): String {
            return renderer.render(parser.parse(md))
        }

        /**
         * 分割元素数量相同的list
         * 来自https://programtip.com/zh/art-48414
         */
        fun <T> averageAssignFixLength(source: List<T>?, splitItemNum: Int): List<List<T>> {
            val result = ArrayList<List<T>>()

            if (source != null && source.run { isNotEmpty() } && splitItemNum > 0) {
                if (source.size <= splitItemNum) {
                    // 源List元素数量小于等于目标分组数量
                    result.add(source)
                } else {
                    // 计算拆分后list数量
                    val splitNum = source.size / splitItemNum + if (source.size % splitItemNum > 0) 1 else 0
                    var value: List<T>?
                    for (i in 0 until splitNum) {
                        if (i < splitNum - 1) {
                            value = source.subList(i * splitItemNum, (i + 1) * splitItemNum)
                        } else {
                            // 最后一组
                            value = source.subList(i * splitItemNum, source.size)
                        }
                        result.add(value)
                    }
                }
            }

            return result
        }
    }
}