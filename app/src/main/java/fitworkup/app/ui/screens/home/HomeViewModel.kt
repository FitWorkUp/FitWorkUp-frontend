package com.fitworkup.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.remote.dto.DailySummaryResponse
import com.fitworkup.app.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val stepsToday: Int = 0,
    val distanceKmToday: Double = 0.0,
    val caloriesToday: Int = 0,
    val fitcoins: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodaySummary()
    }

    fun loadTodaySummary() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result: Result<DailySummaryResponse> = activityRepository.getTodaySummary()
            result.fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        stepsToday = summary.totalSteps,
                        distanceKmToday = summary.totalDistanceKm,
                        caloriesToday = summary.totalCalories,
                        fitcoins = summary.fitcoins,
                        xp = summary.xp,
                        level = summary.level
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage
                    )
                }
            )
        }
    }
}