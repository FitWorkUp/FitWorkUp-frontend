package com.fitworkup.app.ui.screens.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.remote.dto.DailySummaryResponse
import com.fitworkup.app.domain.repository.ActivityRepository
import com.fitworkup.app.util.toLatLngList
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeLocalActivities()
        loadTodaySummary()
    }

    private fun observeLocalActivities() {
        viewModelScope.launch {
            activityRepository.getLocalActivitiesFlow().collectLatest { activities ->
                val today = LocalDate.now()
                val todayActivities = activities.filter { it.date == today }

                val calculatedSteps = todayActivities.sumOf { it.steps }
                val calculatedDistance = todayActivities.sumOf { it.distanceKm }
                val calculatedCalories = (calculatedDistance * 60.0).toInt() // Estimativa baseada na distância percorrida

                _uiState.update { currentState ->
                    currentState.copy(
                        userActivities = activities,
                        stepsToday = if (calculatedSteps > 0) calculatedSteps else currentState.stepsToday,
                        distanceKmToday = if (calculatedDistance > 0.0) calculatedDistance else currentState.distanceKmToday,
                        caloriesToday = if (calculatedCalories > 0) calculatedCalories else currentState.caloriesToday
                    )
                }
                loadTodaySummary()
            }
        }
    }

    fun updateLastWorkoutPath(pathLocations: List<Location>) {
        if (pathLocations.isEmpty()) return

        _uiState.update { currentState ->
            currentState.copy(lastWorkoutPath = pathLocations.toLatLngList())
        }
    }

    fun loadTodaySummary() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result: Result<DailySummaryResponse> = activityRepository.getTodaySummary()
            result.fold(
                onSuccess = { summary ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            stepsToday = if (summary.totalSteps > 0) summary.totalSteps else currentState.stepsToday,
                            distanceKmToday = if (summary.totalDistanceKm > 0.0) summary.totalDistanceKm else currentState.distanceKmToday,
                            caloriesToday = if (summary.totalCalories > 0) summary.totalCalories else currentState.caloriesToday,
                            fitcoins = summary.fitcoins,
                            xp = summary.xp,
                            level = summary.level
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage
                        )
                    }
                }
            )
        }
    }
}