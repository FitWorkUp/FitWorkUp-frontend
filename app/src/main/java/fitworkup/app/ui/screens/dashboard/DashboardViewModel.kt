package com.fitworkup.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.fitworkup.app.domain.repository.UserActivityItem
import com.fitworkup.app.ui.components.DailyRunProgress
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.YearMonth
import javax.inject.Inject

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
    // Injete o repositório de atividades aqui quando disponível
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Coordenadas simuladas para o mini mapa do último percurso
        val lastRoute = listOf(
            LatLng(-12.9714, -38.5014),
            LatLng(-12.9725, -38.5022),
            LatLng(-12.9738, -38.5035)
        )

        _uiState.update { currentState ->
            currentState.copy(
                lastActivityRoutePoints = lastRoute
            )
        }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.update { it.copy(selectedYearMonth = yearMonth) }
    }

    fun onDayFocused(day: Int?) {
        _uiState.update { it.copy(focusedDay = day) }
    }
}