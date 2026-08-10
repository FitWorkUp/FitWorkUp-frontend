package com.fitworkup.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.domain.model.UserActivityItem
import com.fitworkup.app.domain.repository.ActivityRepository
import com.fitworkup.app.ui.components.DailyRunProgress
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val monthlyProgress: List<DailyRunProgress> = emptyList(),
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val focusedDay: Int? = null,
    val focusedDayActivities: List<UserActivityItem> = emptyList(),
    val focusedDayTotalKm: Double = 0.0,
    val lastActivityRoutePoints: List<LatLng> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var localActivities: List<UserActivityItem> = emptyList()

    init {
        observeLocalActivities()
    }

    private fun observeLocalActivities() {
        viewModelScope.launch {
            activityRepository.getLocalActivitiesFlow().collect { activities ->
                localActivities = activities
                rebuildDashboard()
            }
        }
    }

    private fun rebuildDashboard(
        selectedMonth: YearMonth = _uiState.value.selectedYearMonth,
        focusedDay: Int? = _uiState.value.focusedDay
    ) {
        val activitiesInMonth = localActivities.filter {
            YearMonth.from(it.date) == selectedMonth
        }
        val distanceByDay = activitiesInMonth
            .groupBy { it.date.dayOfMonth }
            .mapValues { (_, activities) -> activities.sumOf { it.distanceKm }.toFloat() }

        val today = LocalDate.now()
        val progress = (1..selectedMonth.lengthOfMonth()).map { day ->
            DailyRunProgress(
                day = day,
                distanceKm = distanceByDay[day] ?: 0f,
                isToday = selectedMonth == YearMonth.from(today) && day == today.dayOfMonth,
                isFocused = focusedDay == day
            )
        }

        val focusedActivities = focusedDay?.let { day ->
            activitiesInMonth.filter { it.date.dayOfMonth == day }
        }.orEmpty()

        val latestRoute = localActivities
            .firstOrNull { it.routePoints.isNotEmpty() }
            ?.routePoints
            ?.map { LatLng(it.latitude, it.longitude) }
            .orEmpty()

        _uiState.update {
            it.copy(
                isLoading = false,
                monthlyProgress = progress,
                selectedYearMonth = selectedMonth,
                focusedDay = focusedDay,
                focusedDayActivities = focusedActivities,
                focusedDayTotalKm = focusedActivities.sumOf { activity -> activity.distanceKm },
                lastActivityRoutePoints = latestRoute
            )
        }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        rebuildDashboard(selectedMonth = yearMonth, focusedDay = null)
    }

    fun onDayFocused(day: Int?) {
        rebuildDashboard(focusedDay = day)
    }
}
