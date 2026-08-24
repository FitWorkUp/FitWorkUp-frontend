package com.fitworkup.app.domain.weeklygoal

import com.fitworkup.app.domain.model.UserActivityItem
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyGoalCalculatorTest {
    private val wednesday = LocalDate.of(2026, 8, 19)

    @Test
    fun `counts distinct active dates from monday through sunday`() {
        val activities = listOf(
            activity(1, LocalDate.of(2026, 8, 17)),
            activity(2, LocalDate.of(2026, 8, 17)),
            activity(3, LocalDate.of(2026, 8, 19)),
            activity(4, LocalDate.of(2026, 8, 23))
        )

        assertEquals(3, countWeeklyActiveDays(activities, wednesday))
    }

    @Test
    fun `ignores activities outside current week and activities without movement`() {
        val activities = listOf(
            activity(1, LocalDate.of(2026, 8, 16)),
            activity(2, LocalDate.of(2026, 8, 24)),
            activity(3, LocalDate.of(2026, 8, 18), steps = 0, distanceKm = 0.0),
            activity(4, LocalDate.of(2026, 8, 20), steps = 0, distanceKm = 1.2)
        )

        assertEquals(1, countWeeklyActiveDays(activities, wednesday))
    }

    private fun activity(
        id: Long,
        date: LocalDate,
        steps: Int = 100,
        distanceKm: Double = 0.5
    ) = UserActivityItem(
        id = id,
        type = "WALK",
        distanceKm = distanceKm,
        steps = steps,
        date = date
    )
}
