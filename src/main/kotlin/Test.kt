package top.ntutn

import org.apache.commons.io.FileUtils
import java.io.File

fun main() {
    FileUtils.writeStringToFile(
        File("input/test.out.md"),
        FileUtils.readFileToString(File("input/test.md"), charset("UTF-8")),
        charset("UTF-8")
    )
}