package com.fitworkup.app.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class GpsLocationFilter(
    private val maxAllowedAccuracyMeters: Float = 15.0f,
    private val minDistanceThresholdMeters: Float = 3.0f,
    private val minSpeedThresholdMs: Float = 0.3f // ~1.08 km/h
) {
    private var lastValidLocation: Location? = null

    /**
     * Avalia se uma nova localização do GPS é confiável para ser adicionada ao percurso.
     *
     * @param newLocation Localização bruta recebida do FusedLocationProviderClient.
     * @param isUserWalking Flag indicando se o pedômetro/acelerômetro detectou passos recentes.
     */
    fun shouldAcceptLocation(newLocation: Location, isUserWalking: Boolean): Boolean {
        // 1. Descarta localizações sem precisão ou com raio de erro muito alto (ex: Sinal Fraco > 15m)
        if (!newLocation.hasAccuracy() || newLocation.accuracy > maxAllowedAccuracyMeters) {
            return false
        }

        // 2. Se o sensor físico de passos indica que o usuário está PARADO, ignora qualquer oscilação de GPS
        if (!isUserWalking) {
            return false
        }

        val previousLocation = lastValidLocation ?: run {
            lastValidLocation = newLocation
            return true
        }

        // 3. Calcula a distância real em metros entre o último ponto válido e o novo
        val distanceMeters = previousLocation.distanceTo(newLocation)

        // 4. Descarta pequenas oscilações/ruídos menores que o limiar mínimo (ex: < 3 metros)
        if (distanceMeters < minDistanceThresholdMeters) {
            return false
        }

        // 5. Verifica se a velocidade informada pelo GPS atinge o mínimo para caminhada/corrida
        if (newLocation.hasSpeed() && newLocation.speed < minSpeedThresholdMs) {
            return false
        }

        lastValidLocation = newLocation
        return true
    }

    fun reset() {
        lastValidLocation = null
    }
}