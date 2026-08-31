package com.fitworkup.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fitworkup.app.data.preferences.ReminderSettings
import com.fitworkup.app.worker.DailyReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun update(settings: ReminderSettings) {
        val workManager = WorkManager.getInstance(context)
        if (!settings.activityReminderEnabled && !settings.returnReminderEnabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val now = ZonedDateTime.now()
        var nextRun = now.withHour(settings.reminderHour)
            .withMinute(settings.reminderMinute)
            .withSecond(0)
            .withNano(0)
        if (!nextRun.isAfter(now)) nextRun = nextRun.plusDays(1)
        val initialDelayMinutes = Duration.between(now, nextRun).toMinutes().coerceAtLeast(1)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private companion object {
        const val WORK_NAME = "fitworkup_daily_motivation_reminder"
    }
}
