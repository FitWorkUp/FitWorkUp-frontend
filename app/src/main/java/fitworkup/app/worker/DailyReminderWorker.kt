package com.fitworkup.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fitworkup.app.data.local.dao.ActivityDao
import com.fitworkup.app.data.preferences.ReminderPreferences
import com.fitworkup.app.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderPreferences: ReminderPreferences,
    private val activityDao: ActivityDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = runCatching {
        val settings = reminderPreferences.current()
        val today = LocalDate.now()
        val latestTimestamp = activityDao.getLatestActivityTimestamp()
        val latestActivityDate = latestTimestamp?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val daysWithoutActivity = latestActivityDate?.let {
            ChronoUnit.DAYS.between(it, today).coerceAtLeast(0)
        }

        val shouldSendReturnReminder = settings.returnReminderEnabled &&
            daysWithoutActivity != null &&
            daysWithoutActivity >= settings.returnAfterDays &&
            settings.lastReturnReminderEpochDay != today.toEpochDay()

        when {
            shouldSendReturnReminder -> {
                notificationHelper.showActivityReminder(
                    title = "Que tal retomar seu ritmo?",
                    message = "Uma atividade curta já ajuda você a voltar à sua rotina semanal."
                )
                reminderPreferences.markReturnReminderSent(today.toEpochDay())
            }
            settings.activityReminderEnabled &&
                today.dayOfWeek.value in settings.reminderDays &&
                latestActivityDate != today -> {
                notificationHelper.showActivityReminder(
                    title = "Hora de se movimentar",
                    message = "Sua meta semanal ainda está ao alcance. Que tal uma caminhada curta hoje?"
                )
            }
        }
    }.fold(
        onSuccess = { Result.success() },
        onFailure = { Result.retry() }
    )
}
