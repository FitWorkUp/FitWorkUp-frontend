package com.fitworkup.app.di

import com.fitworkup.app.data.remote.AuthInterceptor
import com.fitworkup.app.BuildConfig
import com.fitworkup.app.data.remote.api.AuthApiService
import com.fitworkup.app.data.remote.api.FriendshipApiService
import com.fitworkup.app.data.remote.api.FitWorkUpApi
import com.fitworkup.app.data.remote.api.UserApiService
import com.fitworkup.app.data.remote.api.StoreApiService
import com.fitworkup.app.data.remote.api.RankingApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFitWorkUpApi(retrofit: Retrofit): FitWorkUpApi {
        return retrofit.create(FitWorkUpApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    // MÉTODO ADICIONADO PARA RESOLVER O ERRO DO UserApiService:
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendshipApiService(retrofit: Retrofit): FriendshipApiService {
        return retrofit.create(FriendshipApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideStoreApiService(retrofit: Retrofit): StoreApiService {
        return retrofit.create(StoreApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRankingApiService(retrofit: Retrofit): RankingApiService {
        return retrofit.create(RankingApiService::class.java)
    }
}
