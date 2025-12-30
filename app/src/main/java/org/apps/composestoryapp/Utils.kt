package org.apps.composestoryapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = inputFormat.parse(dateString) ?: return dateString

        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(
    context: Context,
    onResult: (Double, Double) -> Unit,
    onError: () -> Unit
) {
    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->
        if (location != null) {
            onResult(location.latitude, location.longitude)
        } else {
            onError()
        }
    }.addOnFailureListener {
        onError()
    }
}

fun reverseGeocode(
    context: Context,
    lat: Double,
    lon: Double,
    onResult: (String) -> Unit,
    onError: () -> Unit
) {

    if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
        onError()
        return
    }

    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        val addresses = geocoder.getFromLocation(lat, lon, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]

            val locationName = listOfNotNull(
                address.subLocality,
                address.locality,
                address.adminArea,
                address.countryName
            ).joinToString(", ")

            onResult(locationName)
        } else {
            onError()
        }
    } catch (e: IOException) {
        onError()
    }
}

