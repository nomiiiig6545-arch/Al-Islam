package com.example.data.prayer

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class CalculationMethod(val displayName: String, val shortName: String, val fajrAngle: Double, val ishaAngle: Double) {
    KARACHI("Univ. of Islamic Sciences, Karachi", "Karachi", 18.0, 18.0),
    ISNA("Islamic Society of North America (ISNA)", "ISNA", 15.0, 15.0),
    MWL("Muslim World League (MWL)", "MWL", 18.0, 17.0),
    UMM_AL_QURA("Umm Al-Qura University, Makkah", "Makkah", 18.5, 90.0), // 90 min after Maghrib
    EGYPTIAN("Egyptian General Authority of Survey", "Egyptian", 19.5, 17.5),
    DUBAI("Dubai / UAE Method", "Dubai", 18.2, 18.2),
    GULF("Gulf Region / Kuwait", "Gulf", 19.5, 90.0),
    TEHRAN("Institute of Geophysics, Univ. of Tehran", "Tehran", 17.7, 14.0)
}

enum class JuristicSchool(val displayName: String, val shortName: String, val shadowFactor: Double) {
    SHAFI("Shafi'i / Standard", "Shafi'i", 1.0),
    HANAFI("Hanafi", "Hanafi", 2.0)
}

enum class LocationMode(val displayName: String) {
    AUTO_GPS("AUTO (GPS)"),
    MANUAL_CITY("MANUAL CITY")
}

data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneOffsetHours: Double,
    val isGpsLocation: Boolean = false
) {
    val fullDisplayName: String get() = if (country.isNotBlank()) "$name, $country" else name
}

val DEFAULT_CITIES = listOf(
    CityLocation("Sargodha", "Pakistan", 32.0836, 72.6711, 5.0),
    CityLocation("Lahore", "Pakistan", 31.5204, 74.3587, 5.0),
    CityLocation("Faisalabad", "Pakistan", 31.4504, 73.1350, 5.0),
    CityLocation("Islamabad", "Pakistan", 33.6844, 73.0479, 5.0),
    CityLocation("Rawalpindi", "Pakistan", 33.5651, 73.0169, 5.0),
    CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, 5.0),
    CityLocation("Multan", "Pakistan", 30.1575, 71.5249, 5.0),
    CityLocation("Gujranwala", "Pakistan", 32.1877, 74.1945, 5.0),
    CityLocation("Sialkot", "Pakistan", 32.4945, 74.5229, 5.0),
    CityLocation("Peshawar", "Pakistan", 34.0151, 71.5249, 5.0),
    CityLocation("Quetta", "Pakistan", 30.1798, 66.9750, 5.0),
    CityLocation("Gujrat", "Pakistan", 32.5744, 74.0754, 5.0),
    CityLocation("Bahawalpur", "Pakistan", 29.3544, 71.6911, 5.0),
    CityLocation("Sahiwal", "Pakistan", 30.6682, 73.1114, 5.0),
    CityLocation("Mianwali", "Pakistan", 32.5853, 71.5436, 5.0),
    CityLocation("Jhang", "Pakistan", 31.2781, 72.3317, 5.0),
    CityLocation("Chiniot", "Pakistan", 31.7200, 72.9789, 5.0),
    CityLocation("Rahim Yar Khan", "Pakistan", 28.4195, 70.3024, 5.0),
    CityLocation("Sukkur", "Pakistan", 27.7052, 68.8574, 5.0),
    CityLocation("Larkana", "Pakistan", 27.5590, 68.2120, 5.0),
    CityLocation("Abbottabad", "Pakistan", 34.1688, 73.2215, 5.0),
    CityLocation("Mirpur", "Pakistan", 33.1431, 73.7460, 5.0),
    CityLocation("Muzaffarabad", "Pakistan", 34.3700, 73.4711, 5.0),
    CityLocation("Dera Ghazi Khan", "Pakistan", 30.0489, 70.6455, 5.0),
    CityLocation("Sheikhupura", "Pakistan", 31.7131, 73.9783, 5.0),
    CityLocation("Kasur", "Pakistan", 31.1179, 74.4461, 5.0),
    CityLocation("Okara", "Pakistan", 30.8080, 73.4458, 5.0),
    CityLocation("Jhelum", "Pakistan", 32.9425, 73.7257, 5.0),
    CityLocation("Makkah", "Saudi Arabia", 21.3891, 39.8579, 3.0),
    CityLocation("Madinah", "Saudi Arabia", 24.5247, 39.5692, 3.0),
    CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753, 3.0),
    CityLocation("Dubai", "UAE", 25.2048, 55.2708, 4.0),
    CityLocation("London", "United Kingdom", 51.5074, -0.1278, 0.0),
    CityLocation("New York", "USA", 40.7128, -74.0060, -5.0),
    CityLocation("Toronto", "Canada", 43.6532, -79.3832, -5.0),
    CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, 3.0),
    CityLocation("Cairo", "Egypt", 30.0444, 31.2357, 2.0),
    CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, 8.0),
    CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, 7.0)
)

data class PrayerTimeItem(
    val id: String, // "fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"
    val name: String,
    val formattedTime: String, // e.g. "04:57 am"
    val totalMinutes: Int,
    val isNotificationEnabled: Boolean = true,
    val soundChoice: String = "Adhan" // "Adhan", "Beep", "Silent"
)

data class PrayerTimesResult(
    val fajr: String,
    val sunrise: String,
    val zawalStart: String,
    val zawalEnd: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val sehar: String,
    val upcomingEventName: String,
    val upcomingEventTime: String,
    val upcomingDescription: String,
    val activePrayerId: String?, // "dhuhr" if Dhuhr is currently active
    val currentCityTime: String = ""
)

object HijriCalendarUtils {
    fun getHijriDateString(date: LocalDate, dayOffset: Int = 0): String {
        val targetDate = date.plusDays(dayOffset.toLong())
        // Approximate astronomical Hijri conversion
        val julianDay = targetDate.toEpochDay() + 2440588
        val l = julianDay - 1948440 + 10632
        val n = ((l - 1) / 10631)
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + ((l2 / 5670)) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val month = ((24 * l3) / 709)
        val day = (l3 - (709 * month) / 24).toInt()
        val year = (30 * n + j - 30).toInt()

        val hijriMonths = arrayOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        val monthName = hijriMonths[(month - 1).toInt().coerceIn(0, 11)]
        return "$day $monthName $year AH"
    }

    fun getFormattedGregorianDate(date: LocalDate, dayOffset: Int = 0): String {
        val targetDate = date.plusDays(dayOffset.toLong())
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        return targetDate.format(formatter)
    }

    fun getCombinedDateString(date: LocalDate, dayOffset: Int = 0): String {
        val greg = getFormattedGregorianDate(date, dayOffset)
        val hijri = getHijriDateString(date, dayOffset)
        return "$greg | $hijri"
    }
}
