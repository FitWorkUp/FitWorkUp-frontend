package com.fitworkup.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition

@Composable
fun MiniMapa(modifier: Modifier = Modifier) {
    val localizacao = LatLng(-13.0, -39.0)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(localizacao, 15f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(), // 👈 Use fillMaxSize aqui para herdar o tamanho do pai (Box do layout acima)
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            tiltGesturesEnabled = false,
            myLocationButtonEnabled = false
        )

    )
}
