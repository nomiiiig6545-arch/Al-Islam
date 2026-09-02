package com.example.data.prayer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Result<CityLocation> = withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext Result.failure(Exception("Location service not available"))

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: Location? = null
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    try {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                                bestLocation = loc
                            }
                        }
                    } catch (_: SecurityException) {
                    }
                }
            }

            if (bestLocation == null) {
                return@withContext Result.failure(Exception("No GPS lock available. Please ensure Location is turned on."))
            }

            val lat = bestLocation.latitude
            val lng = bestLocation.longitude

            // Estimate TimeZone offset in hours from longitude / country
            val estimatedTzOffset = when {
                // Pakistan bounds
                lat in 23.0..37.5 && lng in 60.0..78.5 -> 5.0
                // Saudi Arabia / Gulf bounds
                lat in 16.0..32.5 && lng in 34.0..56.0 -> 3.0
                // UAE bounds
                lat in 22.0..26.5 && lng in 51.0..57.0 -> 4.0
                else -> {
                    val systemTz = TimeZone.getDefault()
                    val sysOffset = systemTz.getOffset(System.currentTimeMillis()).toDouble() / (1000.0 * 60.0 * 60.0)
                    // If system offset differs wildly from longitude zone, prefer longitude zone
                    val geoTz = Math.round(lng / 15.0).toDouble()
                    if (Math.abs(sysOffset - geoTz) > 4.0) geoTz else sysOffset
                }
            }

            var cityName = "Current Location"
            var countryName = ""

            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        cityName = address.locality
                            ?: address.subAdminArea
                            ?: address.adminArea
                            ?: "GPS (${String.format(Locale.US, "%.2f, %.2f", lat, lng)})"
                        countryName = address.countryName ?: ""
                    }
                }
            } catch (_: Exception) {
                // If geocoder fails, check nearest default city
            }

            if (cityName == "Current Location" || cityName.startsWith("GPS (")) {
                val nearest = DEFAULT_CITIES.minByOrNull { city ->
                    val dLat = city.latitude - lat
                    val dLng = city.longitude - lng
                    dLat * dLat + dLng * dLng
                }
                if (nearest != null) {
                    cityName = nearest.name
                    countryName = nearest.country
                }
            }

            val cityLocation = CityLocation(
                name = cityName,
                country = countryName,
                latitude = lat,
                longitude = lng,
                timeZoneOffsetHours = estimatedTzOffset,
                isGpsLocation = true
            )

            Result.success(cityLocation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
