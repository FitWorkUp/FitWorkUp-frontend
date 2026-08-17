package com.fitworkup.app.data.repository

import com.fitworkup.app.data.remote.api.FriendshipApiService
import com.fitworkup.app.data.remote.api.UserApiService
import com.fitworkup.app.data.remote.dto.FriendshipRequestDto
import com.fitworkup.app.domain.model.BadgeItem
import com.fitworkup.app.domain.model.FriendItem
import com.fitworkup.app.domain.model.FriendProfileDetails
import com.fitworkup.app.domain.model.UserProfile
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val friendshipApiService: FriendshipApiService
) : ProfileRepository {

    override fun getUserProfile(): Flow<Result<UserProfile>> = flow {
        emit(runCatching {
            val response = userApiService.getMyProfile()
            response.body()?.takeIf { response.isSuccessful }?.toDomain()
                ?: error("Falha ao carregar o perfil (${response.code()}).")
        })
    }

    override fun getFriends(): Flow<Result<List<FriendItem>>> = flow {
        emit(runCatching {
            val currentUser = userApiService.getMyProfile().body()
                ?: error("Sessão de usuário indisponível.")
            val response = friendshipApiService.getFriends()
            val friendships = response.body()?.takeIf { response.isSuccessful }
                ?: error("Falha ao carregar amigos (${response.code()}).")

            friendships.map { friendship ->
                val otherUsername = if (friendship.userId == currentUser.id) {
                    friendship.friendUsername
                } else {
                    friendship.username
                }
                FriendItem(
                    id = friendship.id.toString(),
                    userId = if (friendship.userId == currentUser.id) {
                        friendship.friendId.toString()
                    } else {
                        friendship.userId.toString()
                    },
                    name = otherUsername,
                    tag = otherUsername,
                    level = 1
                )
            }
        })
    }

    override fun getPendingFriendRequests(): Flow<Result<List<FriendItem>>> = flow {
        emit(runCatching {
            val response = friendshipApiService.getPendingRequests()
            val requests = response.body()?.takeIf { response.isSuccessful }
                ?: error("Falha ao carregar solicitações (${response.code()}).")

            requests.map { friendship ->
                FriendItem(
                    id = friendship.id.toString(),
                    userId = friendship.userId.toString(),
                    name = friendship.username,
                    tag = friendship.username,
                    level = 1
                )
            }
        })
    }

    override fun getBadges(): Flow<Result<List<BadgeItem>>> = flow {
        emit(runCatching {
            val response = userApiService.getBadges()
            if (response.isSuccessful) {
                response.body().orEmpty().map { it.toDomain() }
            } else {
                emptyList()
            }
        })
    }

    override suspend fun getFriendProfile(userId: String): Result<FriendProfileDetails> = runCatching {
        val response = userApiService.getPublicProfile(userId.toLong())
        response.body()?.takeIf { response.isSuccessful }?.toDomain()
            ?: error("Não foi possível carregar o perfil do amigo.")
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> = runCatching {
        val response = friendshipApiService.remove(friendId.toLong())
        if (!response.isSuccessful) error("Erro ao remover amigo (${response.code()}).")
    }

    override suspend fun sendFriendRequest(userTagOrEmail: String): Result<Unit> = runCatching {
        val query = userTagOrEmail.trim().removePrefix("@")
        val searchResponse = userApiService.searchUsers(query)
        val candidates = searchResponse.body()?.takeIf { searchResponse.isSuccessful }
            ?: error("Não foi possível pesquisar usuários.")
        val target = candidates.firstOrNull {
            it.username.lowercase(Locale.ROOT) == query.lowercase(Locale.ROOT)
        } ?: candidates.firstOrNull() ?: error("Usuário não encontrado.")

        val response = friendshipApiService.sendRequest(FriendshipRequestDto(target.id))
        if (!response.isSuccessful) error("Não foi possível enviar a solicitação (${response.code()}).")
    }

    override suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        val response = friendshipApiService.accept(friendshipId.toLong())
        if (!response.isSuccessful) error("Não foi possível aceitar a solicitação.")
    }

    override suspend fun rejectFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        val response = friendshipApiService.reject(friendshipId.toLong())
        if (!response.isSuccessful) error("Não foi possível rejeitar a solicitação.")
    }
}
