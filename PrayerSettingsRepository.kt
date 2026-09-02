package com.example.data.prayer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrayerSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)

    private val _selectedCity = MutableStateFlow(getSavedCity())
    val selectedCity: StateFlow<CityLocation> = _selectedCity.asStateFlow()

    private val _calculationMethod = MutableStateFlow(getSavedMethod())
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _juristicSchool = MutableStateFlow(getSavedSchool())
    val juristicSchool: StateFlow<JuristicSchool> = _juristicSchool.asStateFlow()

    private val _locationMode = MutableStateFlow(getSavedLocationMode())
    val locationMode: StateFlow<LocationMode> = _locationMode.asStateFlow()

    private val _cacheStatus = MutableStateFlow(prefs.getString("cache_status", "Loaded from Cache") ?: "Loaded from Cache")
    val cacheStatus: StateFlow<String> = _cacheStatus.asStateFlow()

    private val _notificationStates = MutableStateFlow(getSavedNotificationStates())
    val notificationStates: StateFlow<Map<String, Boolean>> = _notificationStates.asStateFlow()

    private val _soundChoices = MutableStateFlow(getSavedSoundChoices())
    val soundChoices: StateFlow<Map<String, String>> = _soundChoices.asStateFlow()

    private fun getSavedCity(): CityLocation {
        val name = prefs.getString("city_name", "Sargodha") ?: "Sargodha"
        val country = prefs.getString("city_country", "Pakistan") ?: "Pakistan"
        val lat = prefs.getFloat("city_lat", 32.0836f).toDouble()
        val lng = prefs.getFloat("city_lng", 72.6711f).toDouble()
        val tz = prefs.getFloat("city_tz", 5.0f).toDouble()
        val isGps = prefs.getBoolean("city_is_gps", false)
        return CityLocation(name, country, lat, lng, tz, isGps)
    }

    fun saveCity(city: CityLocation, statusMsg: String = "Loaded from Cache") {
        prefs.edit()
            .putString("city_name", city.name)
            .putString("city_country", city.country)
            .putFloat("city_lat", city.latitude.toFloat())
            .putFloat("city_lng", city.longitude.toFloat())
            .putFloat("city_tz", city.timeZoneOffsetHours.toFloat())
            .putBoolean("city_is_gps", city.isGpsLocation)
            .putString("cache_status", statusMsg)
            .apply()
        _selectedCity.value = city
        _cacheStatus.value = statusMsg
    }

    private fun getSavedMethod(): CalculationMethod {
        val name = prefs.getString("calc_method", CalculationMethod.KARACHI.name)
        return try {
            CalculationMethod.valueOf(name!!)
        } catch (e: Exception) {
            CalculationMethod.KARACHI
        }
    }

    fun saveCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString("calc_method", method.name).apply()
        _calculationMethod.value = method
    }

    private fun getSavedSchool(): JuristicSchool {
        val name = prefs.getString("calc_school", JuristicSchool.HANAFI.name)
        return try {
            JuristicSchool.valueOf(name!!)
        } catch (e: Exception) {
            JuristicSchool.HANAFI
        }
    }

    fun saveJuristicSchool(school: JuristicSchool) {
        prefs.edit().putString("calc_school", school.name).apply()
        _juristicSchool.value = school
    }

    private fun getSavedLocationMode(): LocationMode {
        val name = prefs.getString("location_mode", LocationMode.MANUAL_CITY.name)
        return try {
            LocationMode.valueOf(name!!)
        } catch (e: Exception) {
            LocationMode.MANUAL_CITY
        }
    }

    fun saveLocationMode(mode: LocationMode) {
        prefs.edit().putString("location_mode", mode.name).apply()
        _locationMode.value = mode
    }

    fun setCacheStatus(status: String) {
        prefs.edit().putString("cache_status", status).apply()
        _cacheStatus.value = status
    }

    private fun getSavedNotificationStates(): Map<String, Boolean> {
        val keys = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
        val map = mutableMapOf<String, Boolean>()
        keys.forEach { key ->
            map[key] = prefs.getBoolean("notif_enabled_$key", true)
        }
        return map
    }

    fun setNotificationEnabled(prayerId: String, enabled: Boolean) {
        prefs.edit().putBoolean("notif_enabled_$prayerId", enabled).apply()
        _notificationStates.value = _notificationStates.value + (prayerId to enabled)
    }

    private fun getSavedSoundChoices(): Map<String, String> {
        val keys = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
        val map = mutableMapOf<String, String>()
        keys.forEach { key ->
            map[key] = prefs.getString("sound_choice_$key", "Adhan") ?: "Adhan"
        }
        return map
    }

    fun setSoundChoice(prayerId: String, soundChoice: String) {
        prefs.edit().putString("sound_choice_$prayerId", soundChoice).apply()
        _soundChoices.value = _soundChoices.value + (prayerId to soundChoice)
    }
}
