package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@Composable
fun WorkoutMapView(
    currentLocation: LatLng?,
    pathPoints: List<LatLng>,
    hasPermissions: Boolean,
    modifier: Modifier = Modifier
) {
    val defaultLocation = LatLng(-12.9714, -38.5014)
    val targetLatLng = currentLocation ?: pathPoints.lastOrNull() ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, 17f)
    }

    // Acompanhamento em tempo real da posição atual
    LaunchedEffect(currentLocation) {
        if (currentLocation != null && pathPoints.size <= 1) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(currentLocation, 17f)
            )
        }
    }

    // Ajuste dinâmico do enquadramento para exibir toda a rota em progresso
    LaunchedEffect(pathPoints.size) {
        if (pathPoints.size > 1) {
            val boundsBuilder = LatLngBounds.builder()
            pathPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasPermissions && currentLocation != null,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {
            if (pathPoints.size > 1) {
                Polyline(
                    points = pathPoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 12f
                )
            }

            if (currentLocation != null) {
                Marker(
                    state = MarkerState(position = currentLocation),
                    title = "Você está aqui"
                )
            }
        }
    }
}