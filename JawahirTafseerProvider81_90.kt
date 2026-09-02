package com.example.data

object JawahirTafseerProvider81_90 {
    val exactTafseerData = mapOf<String, String>()
    
    fun getExactText(surahNumber: Int, ayahNumber: Int): String? {
        return exactTafseerData["${surahNumber}_${ayahNumber}"]
    }
}
