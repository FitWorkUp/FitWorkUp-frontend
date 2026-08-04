package com.fitworkup.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.domain.repository.ActivityRepository
import com.fitworkup.app.domain.repository.UserActivityItem
import com.fitworkup.app.ui.components.DailyRunProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class DashboardUiState(
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val monthlyProgress: List<DailyRunProgress> = emptyList(),
    val focusedDay: Int? = null,
    val focusedDayActivities: List<UserActivityItem> = emptyList(),
    val focusedDayTotalKm: Double = 0.0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _selectedYearMonth = MutableStateFlow(YearMonth.now())
    private val _focusedDay = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        activityRepository.activitiesFlow,
        _selectedYearMonth,
        _focusedDay
    ) { activities, yearMonth, focusedDay ->
        val today = LocalDate.now()
        val totalDays = yearMonth.lengthOfMonth()

        // Mapeia o gráfico com o somatório de cada dia do mês
        val progressList = (1..totalDays).map { day ->
            val dayDate = yearMonth.atDay(day)
            val isToday = dayDate.isEqual(today)

            val dayKm = activities
                .filter { it.date.isEqual(dayDate) }
                .sumOf { it.distanceKm }
                .toFloat()

            DailyRunProgress(
                day = day,
                distanceKm = dayKm,
                isToday = isToday,
                isFocused = (focusedDay == day)
            )
        }

        // Filtra os treinos específicos do dia que o usuário clicou no gráfico
        val selectedDate = focusedDay?.let { yearMonth.atDay(it) }
        val dayActivities = if (selectedDate != null) {
            activities.filter { it.date.isEqual(selectedDate) }
        } else emptyList()

        val dayTotalKm = dayActivities.sumOf { it.distanceKm }

        DashboardUiState(
            selectedYearMonth = yearMonth,
            monthlyProgress = progressList,
            focusedDay = focusedDay,
            focusedDayActivities = dayActivities,
            focusedDayTotalKm = dayTotalKm
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun onMonthChanged(yearMonth: YearMonth) {
        _selectedYearMonth.value = yearMonth
        _focusedDay.value = null
    }

    fun onDayFocused(day: Int?) {
        _focusedDay.value = if (_focusedDay.value == day) null else day
    }
}