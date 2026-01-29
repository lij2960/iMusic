package com.ijackey.iMusic.di

import com.ijackey.iMusic.data.api.LyricsApi
import com.ijackey.iMusic.data.api.MusicSearchApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LyricsRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MusicSearchRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @LyricsRetrofit
    fun provideLyricsRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://music-api.heheda.top/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @MusicSearchRetrofit
    fun provideMusicSearchRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://music-api.heheda.top/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideLyricsApi(@LyricsRetrofit retrofit: Retrofit): LyricsApi {
        return retrofit.create(LyricsApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideMusicSearchApi(@MusicSearchRetrofit retrofit: Retrofit): MusicSearchApi {
        return retrofit.create(MusicSearchApi::class.java)
    }
}