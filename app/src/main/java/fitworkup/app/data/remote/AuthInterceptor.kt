package com.fitworkup.app.data.remote

import com.fitworkup.app.data.session.TokenStore
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        tokenStore.getTokenBlocking()
            ?.takeIf { it.isNotBlank() }
            ?.let { requestBuilder.header("Authorization", "Bearer $it") }
        return chain.proceed(requestBuilder.build())
    }
}
