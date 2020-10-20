package top.ntutn

import org.slf4j.LoggerFactory
import java.io.File

private val logger by lazy { LoggerFactory.getLogger(Unit::class.java) }

fun main() {
    val parser = MdConfigParser(File("./input/节流和防抖动.md"))
    parser.apply {
        logger.debug(author)
        author = "XYX${(0..Int.MAX_VALUE).random()}"
        saveBack()
    }
}