package com.fitworkup.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@Composable
fun MiniMapa(
    modifier: Modifier = Modifier,
    pathPoints: List<LatLng> = emptyList(),
    initialLocation: LatLng? = null
) {
    val defaultLocation = LatLng(-12.9714, -38.5014)
    val targetLocation = initialLocation ?: pathPoints.firstOrNull() ?: defaultLocation

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLocation, 15f)
    }

    // Centraliza o percurso no mini mapa se houver rota salva
    LaunchedEffect(pathPoints) {
        if (pathPoints.size > 1) {
            val boundsBuilder = LatLngBounds.builder()
            pathPoints.forEach { boundsBuilder.include(it) }
            cameraState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 40)
            )
        } else if (pathPoints.isNotEmpty()) {
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(pathPoints.first(), 15f)
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            tiltGesturesEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    ) {
        if (pathPoints.isNotEmpty()) {
            Polyline(
                points = pathPoints,
                color = MaterialTheme.colorScheme.primary,
                width = 10f
            )

            // Ponto de início do percurso
            Marker(
                state = MarkerState(position = pathPoints.first()),
                title = "Início"
            )

            // Ponto final do percurso
            if (pathPoints.size > 1) {
                Marker(
                    state = MarkerState(position = pathPoints.last()),
                    title = "Fim"
                )
            }
        }
    }
}