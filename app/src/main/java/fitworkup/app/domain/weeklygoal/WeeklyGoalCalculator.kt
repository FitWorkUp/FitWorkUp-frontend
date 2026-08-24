package com.fitworkup.app.domain.weeklygoal

import com.fitworkup.app.domain.model.UserActivityItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

fun countWeeklyActiveDays(
    activities: List<UserActivityItem>,
    today: LocalDate = LocalDate.now()
): Int {
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)

    return activities.asSequence()
        .filter { it.steps > 0 || it.distanceKm > 0.0 }
        .map { it.date }
        .filter { !it.isBefore(weekStart) && !it.isAfter(weekEnd) }
        .distinct()
        .count()
}
