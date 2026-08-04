package com.fitworkup.app.di

import com.fitworkup.app.data.remote.api.ActivityApiService
import com.fitworkup.app.data.remote.api.FakeActivityApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 💡 ALTERE PARA 'false' QUANDO A API OFICIAL ESTIVER ONLINE
    private const val USE_MOCK_API = true

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.fitworkup.com/") // URL futura do servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideActivityApiService(
        retrofit: Retrofit,
        fakeApiService: FakeActivityApiService
    ): ActivityApiService {
        return if (USE_MOCK_API) {
            fakeApiService
        } else {
            retrofit.create(ActivityApiService::class.java)
        }
    }
}