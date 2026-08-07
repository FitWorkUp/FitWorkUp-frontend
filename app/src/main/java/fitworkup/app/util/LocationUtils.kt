package com.fitworkup.app.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Converte uma lista de objetos Location do Android para LatLng do Google Maps SDK.
 */
fun List<Location>.toLatLngList(): List<LatLng> {
    return this.map { location ->
        LatLng(location.latitude, location.longitude)
    }
}