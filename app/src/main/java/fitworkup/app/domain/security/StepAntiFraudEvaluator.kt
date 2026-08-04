package com.fitworkup.app.domain.security

import javax.inject.Inject
import javax.inject.Singleton

data class StepEvaluationResult(
    val isAccepted: Boolean,
    val riskDelta: Int,
    val reason: String?
)

@Singleton
class StepAntiFraudEvaluator @Inject constructor() {

    companion object {
        private const val MAX_WALKING_SPEED_KMH = 25.0 // Acima de 25 km/h indica veículo
        private const val MAX_STEPS_PER_SECOND = 4.5    // Cadência humana máxima plausível
    }

    private var lastEvaluationTimeMs: Long = System.currentTimeMillis()
    private var lastStepCount: Int = 0

    /**
     * Avalia o incremento de passos com base na velocidade média e frequência.
     */
    fun evaluateStepDelta(
        currentTotalSteps: Int,
        currentSpeedKmH: Double
    ): StepEvaluationResult {
        val currentTimeMs = System.currentTimeMillis()
        val timeDeltaSeconds = (currentTimeMs - lastEvaluationTimeMs) / 1000.0
        val stepDelta = currentTotalSteps - lastStepCount

        if (stepDelta <= 0) {
            return StepEvaluationResult(isAccepted = true, riskDelta = 0, reason = null)
        }

        lastEvaluationTimeMs = currentTimeMs
        lastStepCount = currentTotalSteps

        // 1. Validação de Velocidade Máxima
        if (currentSpeedKmH > MAX_WALKING_SPEED_KMH) {
            return StepEvaluationResult(
                isAccepted = false,
                riskDelta = 2,
                reason = "Velocidade excessiva detected (%.1f km/h)".format(currentSpeedKmH)
            )
        }

        // 2. Validação de Cadência de Passos
        if (timeDeltaSeconds > 0) {
            val cadence = stepDelta / timeDeltaSeconds
            if (cadence > MAX_STEPS_PER_SECOND) {
                return StepEvaluationResult(
                    isAccepted = false,
                    riskDelta = 1,
                    reason = "Cadência de passos irrealista (%.1f passos/s)".format(cadence)
                )
            }
        }

        return StepEvaluationResult(isAccepted = true, riskDelta = 0, reason = null)
    }
}