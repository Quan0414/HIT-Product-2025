package com.example.hitproduct.data.api

import android.content.Context
import com.example.hitproduct.common.constants.AuthPrefersConstants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 1. Interceptor chịu trách nhiệm chèn header Authorization
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider()?.let { "Bearer $it" }
        val requestBuilder = original.newBuilder()
        if (token != null) {
            requestBuilder.header("Authorization", token)
        }
        return chain.proceed(requestBuilder.build())
    }
}

object NetworkClient {

    private const val BASE_URL = "https://love-story-app-v1.onrender.com/"

    // 2. Build OkHttpClient kèm AuthInterceptor và logging
    fun provideOkHttpClient(context: Context): OkHttpClient {
        // Sử dụng đúng PREFS_NAME và ACCESS_TOKEN từ AuthPrefersConstants
        val prefs = context.getSharedPreferences(
            AuthPrefersConstants.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val authInterceptor = AuthInterceptor {
            prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, null)
        }

        // Logging để debug header và body
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    // 3. Build Retrofit với client trên
    fun provideRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 4. Cung cấp ApiService đã gắn interceptor
    fun provideApiService(context: Context): ApiService {
        return provideRetrofit(context).create(ApiService::class.java)
    }
}
