package com.hehe.awa.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class UserLocation(
    val latitude: Double,
    val longitude: Double
)

suspend fun getCurrentLocation(context: Context): UserLocation? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val hasFineLocation = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarseLocation = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasFineLocation && !hasCoarseLocation) {
        return null
    }

    return suspendCancellableCoroutine { continuation ->
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        var location: Location? = null

        val locationListener = android.location.LocationListener { loc ->
            if (loc != null) {
                location = loc
                continuation.resume(UserLocation(loc.latitude, loc.longitude))
            }
        }

        try {
            if (isGpsEnabled && hasFineLocation) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }

            if (isNetworkEnabled && (hasFineLocation || hasCoarseLocation)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }

            val lastKnownGpsLocation = if (isGpsEnabled && hasFineLocation) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null

            val lastKnownNetworkLocation = if (isNetworkEnabled && (hasFineLocation || hasCoarseLocation)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            location = when {
                lastKnownGpsLocation != null && lastKnownNetworkLocation != null -> {
                    if (lastKnownGpsLocation.accuracy > lastKnownNetworkLocation.accuracy) {
                        lastKnownNetworkLocation
                    } else {
                        lastKnownGpsLocation
                    }
                }
                lastKnownGpsLocation != null -> lastKnownGpsLocation
                lastKnownNetworkLocation != null -> lastKnownNetworkLocation
                else -> null
            }

            if (location != null) {
                locationManager.removeUpdates(locationListener)
                continuation.resume(UserLocation(location!!.latitude, location!!.longitude))
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }

        continuation.invokeOnCancellation {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: Exception) {
            }
        }
    }
}

