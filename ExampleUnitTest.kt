package com.example

import org.junit.Assert.*
import org.junit.Test

data class TestRecentSurah(
    val reciterId: String,
    val surahNumber: Int,
    val timestamp: Long
)

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRecentlyPlayedParsing() {
        val raw = "ar.alajamy:1:1000,ur.khan:55:3000,ar.alafasy:36:2000"
        val list = raw.split(",")
            .mapNotNull { item ->
                val parts = item.split(":")
                if (parts.size >= 3) {
                    TestRecentSurah(
                        reciterId = parts[0],
                        surahNumber = parts[1].toIntOrNull() ?: return@mapNotNull null,
                        timestamp = parts[2].toLongOrNull() ?: 0L
                    )
                } else {
                    null
                }
            }
            .sortedByDescending { it.timestamp }

        assertEquals(3, list.size)
        // Verify sorted order (highest timestamp first)
        assertEquals("ur.khan", list[0].reciterId)
        assertEquals(55, list[0].surahNumber)
        assertEquals(3000L, list[0].timestamp)

        assertEquals("ar.alafasy", list[1].reciterId)
        assertEquals(36, list[1].surahNumber)
        assertEquals(2000L, list[1].timestamp)

        assertEquals("ar.alajamy", list[2].reciterId)
        assertEquals(1, list[2].surahNumber)
        assertEquals(1000L, list[2].timestamp)
    }
}
