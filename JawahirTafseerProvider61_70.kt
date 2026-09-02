package com.example.data

object JawahirTafseerProvider61_70 {
    val exactTafseerData = mapOf<String, String>()
    
    fun getExactText(surahNumber: Int, ayahNumber: Int): String? {
        return exactTafseerData["${surahNumber}_${ayahNumber}"]
    }
}
