package top.ntutn

import org.slf4j.LoggerFactory
import java.io.File

private val logger by lazy { LoggerFactory.getLogger(Unit::class.java) }

fun main() {
    val parser = MdConfigParser(File("./input/节流和防抖动.md"))
    parser.apply {
        logger.debug("$attributes")
        logger.debug(title)
        logger.debug(author)
        logger.debug("$tags")
        logger.debug(summary)
        logger.debug(publishDate.toString())
        logger.debug(editDate.toString())
    }
}