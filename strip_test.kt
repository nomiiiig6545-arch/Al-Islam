fun main() {
    val text = "\u0628\u0650\u0633\u0652\u0645\u0650 \u0671\u0644\u0644\u0651\u064e\u0647\u0650 \u0671\u0644\u0631\u0651\u064e\u062d\u0652\u0645\u064e\u0670\u0646\u0650 \u0671\u0644\u0631\u0651\u064e\u062d\u0650\u064a\u0645\u0650 \u0627\u0644\u0653\u0645\u0653"
    
    var strippedText = text
    val bismillahPrefix = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ "
    if (strippedText.startsWith(bismillahPrefix)) {
        strippedText = strippedText.removePrefix(bismillahPrefix)
    }
    println("Original: " + text)
    println("Stripped: " + strippedText)
}
