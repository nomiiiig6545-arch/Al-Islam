package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object TajweedColorizer {
    val COLOR_GREEN = Color(0xFF1B8A38) // Tafkheem / Bold letters (ص ض ط ظ خ غ ق)
    val COLOR_RED = Color(0xFFD32F2F)   // Ghunna & Madd (نّ مّ ~ ٓ)
    val COLOR_BLUE = Color(0xFF1976D2)  // Qalqala (ق ط ب ج د with jazam / sukun)
    val COLOR_ORANGE = Color(0xFFE65100)// Ikhfa / Special rules

    private val TAFKHEEM_LETTERS = setOf('ص', 'ض', 'ط', 'ظ', 'خ', 'غ', 'ق')
    private val QALQALA_LETTERS = setOf('ق', 'ط', 'ب', 'ج', 'د')

    /**
     * Parses Arabic text and applies authentic 16-Line Tajweed color coding to letters.
     */
    fun formatTajweedText(
        text: String,
        defaultTextColor: Color,
        isNightMode: Boolean = false
    ): AnnotatedString {
        return buildAnnotatedString {
            val green = if (isNightMode) Color(0xFF4ADE80) else COLOR_GREEN
            val red = if (isNightMode) Color(0xFFF87171) else COLOR_RED
            val blue = if (isNightMode) Color(0xFF60A5FA) else COLOR_BLUE

            var i = 0
            while (i < text.length) {
                val char = text[i]

                // Check for Madd sign (ٓ or ~)
                if (char == 'ٓ' || char == 'ۤ' || char == '~') {
                    pushStyle(SpanStyle(color = red, fontWeight = FontWeight.ExtraBold))
                    append(char)
                    pop()
                    i++
                    continue
                }

                // Check for Shaddah on Noon / Meem (Ghunna)
                if ((char == 'ن' || char == 'م') && (i + 1 < text.length && text[i + 1] == 'ّ')) {
                    pushStyle(SpanStyle(color = red, fontWeight = FontWeight.Bold))
                    append(char)
                    append(text[i + 1])
                    pop()
                    i += 2
                    continue
                }

                // Check for Tafkheem letters
                if (TAFKHEEM_LETTERS.contains(char)) {
                    pushStyle(SpanStyle(color = green, fontWeight = FontWeight.Bold))
                    append(char)
                    pop()
                    i++
                    continue
                }

                // Check for Qalqala letters with Sukun/Jazam
                if (QALQALA_LETTERS.contains(char) && (i + 1 < text.length && (text[i + 1] == 'ْ' || text[i + 1] == 'ۡ'))) {
                    pushStyle(SpanStyle(color = blue, fontWeight = FontWeight.Bold))
                    append(char)
                    append(text[i + 1])
                    pop()
                    i += 2
                    continue
                }

                // Default letter
                pushStyle(SpanStyle(color = defaultTextColor))
                append(char)
                pop()
                i++
            }
        }
    }
}
