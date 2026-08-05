package com.fitworkup.app.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MiniMapa(
    routePoints: List<LatLng>,
    modifier: Modifier = Modifier
) {
    if (routePoints.isEmpty()) {
        // Estado visual seguro quando não há percurso gravado
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum percurso registrado recentemente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val cameraPositionState = rememberCameraPositionState()

        // Ajusta o enquadramento do mapa para cobrir todo o percurso
        LaunchedEffect(routePoints) {
            if (routePoints.size > 1) {
                val builder = LatLngBounds.Builder()
                routePoints.forEach { builder.include(it) }
                val bounds = builder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 32)
                )
            } else if (routePoints.size == 1) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(routePoints.first(), 15f)
            }
        }

        GoogleMap(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = false,
                rotationGesturesEnabled = false,
                tiltGesturesEnabled = false,
                compassEnabled = false
            )
        ) {
            Polyline(
                points = routePoints,
                color = MaterialTheme.colorScheme.primary,
                width = 8f
            )
        }
    }
}