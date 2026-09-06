package com.fitworkup.app.ui.screens.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.remote.dto.DailySummaryResponse
import com.fitworkup.app.data.preferences.WeeklyGoalPreferences
import com.fitworkup.app.domain.repository.ActivityRepository
import com.fitworkup.app.domain.repository.StoreRepository
import com.fitworkup.app.data.repository.ProfileRepository
import com.fitworkup.app.domain.weeklygoal.countWeeklyActiveDays
import com.fitworkup.app.util.toLatLngList
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val profileRepository: ProfileRepository,
    private val weeklyGoalPreferences: WeeklyGoalPreferences,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeLocalActivities()
        observeWeeklyGoal()
        loadUserProfile()
        loadTodaySummary()
        loadActiveModifiers()
    }

    private fun observeWeeklyGoal() {
        viewModelScope.launch {
            weeklyGoalPreferences.settings.collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        weeklyGoalEnabled = settings.enabled,
                        weeklyGoalDays = settings.targetDays
                    )
                }
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            profileRepository.getUserProfile().first().onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        userName = profile.name,
                        avatarKey = profile.avatarKey
                    )
                }
            }
        }
    }

    fun loadActiveModifiers() {
        viewModelScope.launch {
            storeRepository.getActiveModifiers().onSuccess { modifiers ->
                _uiState.update { it.copy(activeModifiers = modifiers) }
            }
        }
    }

    private fun observeLocalActivities() {
        viewModelScope.launch {
            activityRepository.getLocalActivitiesFlow().collectLatest { activities ->
                val today = LocalDate.now()
                val todayActivities = activities.filter { it.date == today }

                val calculatedSteps = todayActivities.sumOf { it.steps }
                val calculatedDistance = todayActivities.sumOf { it.distanceKm }
                val activeDays = countWeeklyActiveDays(activities, today)

                _uiState.update { currentState ->
                    currentState.copy(
                        userActivities = activities,
                        weeklyActiveDays = activeDays,
                        stepsToday = if (calculatedSteps > 0) calculatedSteps else currentState.stepsToday,
                        distanceKmToday = if (calculatedDistance > 0.0) calculatedDistance else currentState.distanceKmToday
                    )
                }
                loadTodaySummary()
            }
        }
    }

    fun updateWeeklyGoal(enabled: Boolean, targetDays: Int) {
        viewModelScope.launch {
            weeklyGoalPreferences.update(enabled, targetDays)
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
