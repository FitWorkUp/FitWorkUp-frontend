package com.fitworkup.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    // Injetar TokenManager/DataStore contendo o JWT salvo no login
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Exemplo: resgatar JWT do armazenamento criptografado
        val jwtToken = getSavedJwtToken()

        if (!jwtToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $jwtToken")
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun getSavedJwtToken(): String? {
        // Retorna o token persistido após login no AuthController
        return null
    }
}