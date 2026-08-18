package com.fitworkup.app.data.remote

import com.fitworkup.app.data.session.TokenStore
import com.fitworkup.app.data.session.SessionManager
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val isPublicAuthRequest = originalRequest.url.encodedPath.startsWith("/api/v1/auth/")
        val currentToken = if (isPublicAuthRequest) null else {
            tokenStore.getTokenBlocking()?.takeIf(String::isNotBlank)
        }

        val authenticatedRequest = originalRequest.newBuilder().apply {
            currentToken?.let { header("Authorization", "Bearer $it") }
        }.build()

        val response = chain.proceed(authenticatedRequest)

        // Somente 401 em uma chamada autenticada significa sessão inválida/expirada.
        // Falta de rede, 403, 422 e erros 5xx não removem a sessão do usuário.
        if (response.code == 401 && currentToken != null) {
            sessionManager.invalidateIfCurrent(currentToken)
        }

        return response
    }
}
