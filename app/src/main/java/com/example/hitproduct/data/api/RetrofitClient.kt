package com.example.hitproduct.data.api

import com.example.hitproduct.common.constants.ApiConstants
import com.example.hitproduct.util.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    companion object {
        private const val BASE_URL = ApiConstants.BASE_URL  // hoặc gán thẳng "https://your.api/"
        private var INSTANCE: Retrofit? = null

        /** Trả về singleton Retrofit instance */
        fun getInstance(): Retrofit =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: createRetrofit().also { INSTANCE = it }
            }

        private fun createRetrofit(): Retrofit {

            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client: OkHttpClient = OkHttpClient.Builder()
                .readTimeout(Constant.READ_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(Constant.CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
    }
}
