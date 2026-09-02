import java.io.File

fun main() {
    val file = File("app/src/main/assets/quran/quran_text.json")
    println("File length: ${file.length()}")
    val text = file.readText(Charsets.UTF_8)
    println("Text length: ${text.length}")
}
