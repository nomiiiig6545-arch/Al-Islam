package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {
    @Test
    fun testActivityLaunches() {
        try {
            val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
            println("Activity launched successfully.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
