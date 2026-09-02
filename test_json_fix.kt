import java.io.File

fun main() {
    val file = File("app/src/main/assets/quran/quran_text.json")
    val text = file.readText(Charsets.UTF_8)
    println("Original text length: ${text.length}")
    
    var fixedText = text
    if (fixedText.endsWith("وَمَنْ")) {
        fixedText += "\"}]}"
        println("Appended closing brackets")
    }
    
    // We can't use org.json.JSONObject here easily, but we can verify if it ends correctly.
    println("Ends with: " + fixedText.substring(fixedText.length - 20))
}
