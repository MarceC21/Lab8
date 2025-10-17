package com.example.lab8.remote

import android.util.Log

import com.example.lab8.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor

private const val BASE_URL = "https://api.pexels.com/v1/"
object PexelsService {

    private val auth = Interceptor { chain ->
        val key = BuildConfig.PEXELS_API_KEY

        if (key.isBlank()) {
            Log.e("PEXELS_KEY", "API Key is missing!")
        } else if (BuildConfig.DEBUG) {
            Log.d("PEXELS_KEY", "Key loaded: ${key.take(10)}...")
        }

        val req = chain.request().newBuilder()
            .addHeader("Authorization", key)
            .build()
        chain.proceed(req)
    }

    private val logger = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(logger)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val api: PexelsApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()
        .create(PexelsApi::class.java)
}