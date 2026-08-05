package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun WorkoutMapView(
    currentLocation: LatLng?,
    pathPoints: List<LatLng>,
    hasPermissions: Boolean,
    modifier: Modifier = Modifier
) {
    val defaultLocation = LatLng(-12.9714, -38.5014)
    val targetLatLng = currentLocation ?: pathPoints.lastOrNull() ?: defaultLocation
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, 17f)
    }

    // Acompanhamento em tempo real da posição atual na primeira leitura
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
            try {
                val boundsBuilder = LatLngBounds.builder()
                pathPoints.forEach { boundsBuilder.include(it) }
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 60)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                // Desenha a linha do percurso percorrido
                if (pathPoints.size > 1) {
                    Polyline(
                        points = pathPoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 12f
                    )
                }

                // Marcador da posição atual
                if (currentLocation != null) {
                    Marker(
                        state = MarkerState(position = currentLocation),
                        title = "Sua Posição"
                    )
                }
            }

            // 🎯 Botão Flutuante de Localização / Centralização
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val pos = currentLocation ?: pathPoints.lastOrNull()
                        if (pos != null) {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(pos, 17f)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Minha Localização"
                )
            }
        }
    }
}