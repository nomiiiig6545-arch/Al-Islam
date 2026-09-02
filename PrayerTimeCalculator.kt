package com.example.data.prayer

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

object PrayerTimeCalculator {

    fun calculate(
        date: LocalDate,
        location: CityLocation,
        method: CalculationMethod,
        school: JuristicSchool = JuristicSchool.HANAFI
    ): PrayerTimesResult {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        // Julian Date
        val jd = julianDate(year, month, day) - location.longitude / (15.0 * 24.0)

        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val fixRa = fixHour(ra)

        val eqT = q / 15.0 - fixRa
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))

        // Dhuhr
        val dhuhrUtc = 12.0 + (15.0 * location.timeZoneOffsetHours - location.longitude) / 15.0 - eqT
        val dhuhrLocal = fixHour(dhuhrUtc)

        // Sunrise & Sunset (Angle = 0.833 below horizon)
        val sunriseAngle = 0.833
        val sunriseHourAngle = sunAngleTime(sunriseAngle, decl, location.latitude)
        val sunriseLocal = dhuhrLocal - sunriseHourAngle / 15.0
        val sunsetLocal = dhuhrLocal + sunriseHourAngle / 15.0

        // Fajr (Angle below horizon)
        val fajrHourAngle = sunAngleTime(method.fajrAngle, decl, location.latitude)
        val fajrLocal = dhuhrLocal - fajrHourAngle / 15.0

        // Asr calculation based on Juristic School (Shafi'i: 1x shadow factor, Hanafi: 2x shadow factor)
        // Shadow formula: arccot(t + tan(|lat - decl|)) where t is shadow factor (1 for Shafi, 2 for Hanafi)
        val asrFactor = school.shadowFactor
        val asrAltitude = Math.toDegrees(atan(1.0 / (asrFactor + tan(Math.toRadians(abs(location.latitude - decl))))))
        val asrHourAngle = sunAltitudeHourAngle(asrAltitude, decl, location.latitude)
        val asrLocal = dhuhrLocal + asrHourAngle / 15.0

        // Maghrib (Sunset + 2 mins buffer)
        val maghribLocal = sunsetLocal + (2.0 / 60.0)

        // Isha
        val ishaLocal = if (method == CalculationMethod.UMM_AL_QURA || method == CalculationMethod.GULF) {
            maghribLocal + 1.5 // 90 mins after Maghrib
        } else {
            val ishaHourAngle = sunAngleTime(method.ishaAngle, decl, location.latitude)
            dhuhrLocal + ishaHourAngle / 15.0
        }

        // Zawal (Makruh) Timing: starts ~10 mins before Solar Noon (Dhuhr) and ends at Dhuhr
        val zawalStartLocal = dhuhrLocal - (10.0 / 60.0)
        val zawalEndLocal = dhuhrLocal

        // Sehar time (ends 10 mins before Fajr)
        val seharLocal = fajrLocal - (10.0 / 60.0)

        // Format to string
        val fajrStr = formatTime(fajrLocal)
        val sunriseStr = formatTime(sunriseLocal)
        val zawalStartStr = formatTime(zawalStartLocal)
        val zawalEndStr = formatTime(zawalEndLocal)
        val dhuhrStr = formatTime(dhuhrLocal)
        val asrStr = formatTime(asrLocal)
        val maghribStr = formatTime(maghribLocal)
        val ishaStr = formatTime(ishaLocal)
        val seharStr = formatTime(seharLocal)

        // Calculate current local time in the selected city's timezone
        val tzTotalSeconds = (location.timeZoneOffsetHours * 3600.0).roundToInt().coerceIn(-64800, 64800)
        val cityZoneOffset = ZoneOffset.ofTotalSeconds(tzTotalSeconds)
        val cityZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), cityZoneOffset)
        val cityLocalTime = cityZonedDateTime.toLocalTime()
        val nowMinutes = cityLocalTime.hour * 60 + cityLocalTime.minute

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val currentCityTimeStr = cityLocalTime.format(timeFormatter).lowercase()

        val fajrMin = timeToMinutes(fajrLocal)
        val sunriseMin = timeToMinutes(sunriseLocal)
        val dhuhrMin = timeToMinutes(dhuhrLocal)
        val asrMin = timeToMinutes(asrLocal)
        val maghribMin = timeToMinutes(maghribLocal)
        val ishaMin = timeToMinutes(ishaLocal)
        val seharMin = timeToMinutes(seharLocal)

        // Determine active prayer & upcoming event accurately based on city's time
        var activeId: String? = null
        var upcomingName = "Fajr"
        var upcomingTime = fajrStr
        var upcomingDesc = "Upcoming Fajr Time will start soon."

        if (nowMinutes in fajrMin until sunriseMin) {
            activeId = "fajr"
            upcomingName = "Sunrise"
            upcomingTime = sunriseStr
            upcomingDesc = "Sunrise will begin soon."
        } else if (nowMinutes in sunriseMin until dhuhrMin) {
            activeId = "sunrise"
            upcomingName = "Dhuhr"
            upcomingTime = dhuhrStr
            upcomingDesc = "Dhuhr Prayer will start soon."
        } else if (nowMinutes in dhuhrMin until asrMin) {
            activeId = "dhuhr"
            upcomingName = "Asr"
            upcomingTime = asrStr
            upcomingDesc = "Asr Prayer will start soon."
        } else if (nowMinutes in asrMin until maghribMin) {
            activeId = "asr"
            upcomingName = "Maghrib"
            upcomingTime = maghribStr
            upcomingDesc = "Maghrib (Iftar) will start soon."
        } else if (nowMinutes in maghribMin until ishaMin) {
            activeId = "maghrib"
            upcomingName = "Isha'a"
            upcomingTime = ishaStr
            upcomingDesc = "Isha Prayer will start soon."
        } else if (nowMinutes >= ishaMin || nowMinutes < seharMin) {
            activeId = "isha"
            upcomingName = "Fajr"
            upcomingTime = fajrStr
            upcomingDesc = "Fajr Prayer will start tomorrow morning."
        } else {
            // between sehar and fajr
            activeId = "sehar"
            upcomingName = "Fajr"
            upcomingTime = fajrStr
            upcomingDesc = "Fajr Prayer will start in a few minutes."
        }

        return PrayerTimesResult(
            fajr = fajrStr,
            sunrise = sunriseStr,
            zawalStart = zawalStartStr,
            zawalEnd = zawalEndStr,
            dhuhr = dhuhrStr,
            asr = asrStr,
            maghrib = maghribStr,
            isha = ishaStr,
            sehar = seharStr,
            upcomingEventName = upcomingName,
            upcomingEventTime = upcomingTime,
            upcomingDescription = upcomingDesc,
            activePrayerId = activeId,
            currentCityTime = currentCityTimeStr
        )
    }

    private fun timeToMinutes(hoursFraction: Double): Int {
        val h = fixHour(hoursFraction)
        val hours = h.toInt()
        val minutes = ((h - hours) * 60).toInt()
        return hours * 60 + minutes
    }

    private fun formatTime(hoursFraction: Double): String {
        val h = fixHour(hoursFraction)
        var hours = h.toInt()
        var minutes = ((h - hours) * 60).toInt()
        if (minutes >= 60) {
            hours += 1
            minutes -= 60
        }
        val localTime = LocalTime.of(hours % 24, minutes)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return localTime.format(formatter).lowercase()
    }

    // For depression angle below horizon (Fajr, Isha, Sunrise, Sunset)
    private fun sunAngleTime(angle: Double, decl: Double, lat: Double): Double {
        val top = -sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))
        val bottom = cos(Math.toRadians(lat)) * cos(Math.toRadians(decl))
        val cosH = top / bottom
        return if (cosH > 1.0) 0.0 else if (cosH < -1.0) 180.0 else Math.toDegrees(acos(cosH))
    }

    // For positive altitude above horizon (Asr)
    private fun sunAltitudeHourAngle(altitude: Double, decl: Double, lat: Double): Double {
        val top = sin(Math.toRadians(altitude)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))
        val bottom = cos(Math.toRadians(lat)) * cos(Math.toRadians(decl))
        val cosH = top / bottom
        return if (cosH > 1.0) 0.0 else if (cosH < -1.0) 180.0 else Math.toDegrees(acos(cosH))
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var b = angle - 360.0 * floor(angle / 360.0)
        if (b < 0) b += 360.0
        return b
    }

    private fun fixHour(hour: Double): Double {
        var b = hour - 24.0 * floor(hour / 24.0)
        if (b < 0) b += 24.0
        return b
    }
}
