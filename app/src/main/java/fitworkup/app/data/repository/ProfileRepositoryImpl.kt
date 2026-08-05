package com.fitworkup.app.data.repository

import com.fitworkup.app.data.remote.api.UserApiService
import com.fitworkup.app.domain.model.BadgeItem
import com.fitworkup.app.domain.model.FriendItem
import com.fitworkup.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: UserApiService
) : ProfileRepository {

    // Substituir "me" pelo ID do usuário logado mantido na sessão local
    private val currentUserId = "me"

    override fun getUserProfile(): Flow<Result<UserProfile>> = flow {
        try {
            val response = apiService.getUserProfile(currentUserId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.toDomain()))
            } else {
                emit(Result.failure(Exception("Erro HTTP: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getFriends(): Flow<Result<List<FriendItem>>> = flow {
        try {
            val response = apiService.getFriends(currentUserId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Erro ao buscar amigos.")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getBadges(): Flow<Result<List<BadgeItem>>> = flow {
        try {
            val response = apiService.getBadges(currentUserId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Erro ao buscar conquistas.")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            val response = apiService.removeFriend(currentUserId, friendId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erro ao remover amigo no servidor."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendFriendRequest(userTagOrEmail: String): Result<Unit> {
        return try {
            val response = apiService.sendFriendRequest(userTagOrEmail)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Usuário não encontrado."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}