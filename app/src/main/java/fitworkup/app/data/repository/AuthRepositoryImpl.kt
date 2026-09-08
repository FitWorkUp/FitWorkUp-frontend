package com.fitworkup.app.data.repository

import com.fitworkup.app.data.remote.api.AuthApiService
import com.fitworkup.app.data.remote.dto.LoginRequestDto
import com.fitworkup.app.data.remote.dto.ForgotPasswordRequestDto
import com.fitworkup.app.data.remote.dto.GoogleLoginRequestDto
import com.fitworkup.app.data.remote.dto.RegisterRequestDto
import com.fitworkup.app.data.remote.dto.ResetPasswordRequestDto
import com.fitworkup.app.data.session.TokenStore
import com.fitworkup.app.domain.model.UserProfile
import com.fitworkup.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import retrofit2.Response

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(identifier: String, password: String): Result<UserProfile> = runCatching {
        val response = authApiService.login(LoginRequestDto(identifier.trim(), password))
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            throw IllegalStateException("Não foi possível entrar. Verifique suas credenciais.")
        }
        tokenStore.saveToken(body.accessToken)
        body.user.toDomain()
    }

    override suspend fun register(username: String, email: String, password: String): Result<UserProfile> = runCatching {
        val registerResponse = authApiService.register(
            RegisterRequestDto(username.trim(), email.trim().lowercase(), password)
        )
        if (!registerResponse.isSuccessful || registerResponse.body() == null) {
            throw IllegalStateException("Não foi possível criar a conta.")
        }
        login(email, password).getOrThrow()
    }

    override suspend fun loginWithGoogle(idToken: String): Result<UserProfile> = runCatching {
        val response = authApiService.loginWithGoogle(GoogleLoginRequestDto(idToken))
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            throw IllegalStateException(
                response.apiErrorMessage("Não foi possível entrar com o Google (${response.code()}).")
            )
        }
        tokenStore.saveToken(body.accessToken)
        body.user.toDomain()
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> = runCatching {
        val response = authApiService.forgotPassword(
            ForgotPasswordRequestDto(email.trim().lowercase())
        )
        if (!response.isSuccessful) {
            throw IllegalStateException("Não foi possível enviar o código. Verifique a conexão.")
        }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val response = authApiService.resetPassword(
            ResetPasswordRequestDto(
                email = email.trim().lowercase(),
                code = code.trim(),
                newPassword = newPassword
            )
        )
        if (!response.isSuccessful) {
            throw IllegalArgumentException("Código inválido, expirado ou com muitas tentativas.")
        }
    }

    override suspend fun logout() {
        tokenStore.clear()
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
    }

    override suspend fun hasSession(): Boolean = tokenStore.hasToken()

    private fun Response<*>.apiErrorMessage(fallback: String): String {
        val rawBody = errorBody()?.string() ?: return fallback
        return runCatching { JSONObject(rawBody).optString("message") }
            .getOrNull()
            ?.takeIf(String::isNotBlank)
            ?: fallback
    }
}
