package com.fitworkup.app.data.repository

import com.fitworkup.app.data.local.dao.ActivityDao
import com.fitworkup.app.data.local.entity.ActivityEntity
import com.fitworkup.app.data.remote.api.FitWorkUpApi
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.DailySummaryResponse
import com.fitworkup.app.domain.model.UserActivityItem
import com.fitworkup.app.domain.repository.ActivityRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityRepositoryImpl @Inject constructor(
    private val api: FitWorkUpApi,
    private val activityDao: ActivityDao
) : ActivityRepository {

    private val gson = Gson()

    // Escopo assíncrono para tarefas de segundo plano que não travam a UI
    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    override suspend fun registerActivity(request: ActivityRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. PERSISTÊNCIA INSTANTÂNEA NO BANCO LOCAL (SQLite via Room)
            val localEntity = ActivityEntity(
                type = request.type,
                distanceKm = request.distanceKm,
                steps = request.steps,
                avgSpeed = request.avgSpeed,
                acceptedSteps = request.acceptedSteps,
                heldSteps = request.heldSteps,
                riskScore = request.riskScore,
                routeJson = gson.toJson(request.routePoints),
                timestamp = System.currentTimeMillis(),
                isSynced = false
            )

            val insertedId = activityDao.insertActivity(localEntity)

            // 2. SINCRONIZAÇÃO ASSÍNCRONA COM O BACKEND (Não bloqueia a UI!)
            backgroundScope.launch {
                try {
                    val response = api.registerActivity(request)
                    if (response.isSuccessful) {
                        activityDao.markAsSynced(insertedId)
                    }
                } catch (_: Exception) {
                    // Servidor offline: o treino continua salvo localmente para sincronizar depois
                }
            }

            // Retorna sucesso imediatamente (destrava o botão da tela em milissegundos)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodaySummary(): Result<DailySummaryResponse> {
        return try {
            val response = api.getTodaySummary()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                getTodaySummaryFromLocal()
            }
        } catch (e: Exception) {
            getTodaySummaryFromLocal()
        }
    }

    private suspend fun getTodaySummaryFromLocal(): Result<DailySummaryResponse> = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val activities = activityDao.getAllActivities().filter { entity ->
            Instant.ofEpochMilli(entity.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == today
        }
        val totalSteps = activities.sumOf { it.steps }
        val totalDistance = activities.sumOf { it.distanceKm }
        val estimatedCalories = maxOf(
            (totalDistance * 60.0).toInt(),
            (totalSteps * 0.04).toInt()
        )

        Result.success(
            DailySummaryResponse(
                totalSteps = totalSteps,
                totalDistanceKm = totalDistance,
                totalCalories = estimatedCalories,
                fitcoins = totalSteps / 100,
                xp = totalSteps / 10,
                level = 1
            )
        )
    }

    override fun getLocalActivitiesFlow(): Flow<List<UserActivityItem>> {
        return activityDao.getAllActivitiesFlow().map { entities ->
            entities.map { entity ->
                UserActivityItem(
                    id = entity.id,
                    type = entity.type,
                    distanceKm = entity.distanceKm,
                    steps = entity.steps,
                    date = Instant.ofEpochMilli(entity.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(),
                    routePoints = runCatching {
                        val routeType = object : TypeToken<List<com.fitworkup.app.domain.model.RoutePoint>>() {}.type
                        gson.fromJson<List<com.fitworkup.app.domain.model.RoutePoint>>(entity.routeJson, routeType)
                    }.getOrDefault(emptyList())
                )
            }
        }
    }
}
