package com.onethefull.dasomcontents.data.api

import android.util.Log
import com.onethefull.dasomcontents.App
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by sjw on 2021/11/10
 */
object RetrofitBuilder {
    private const val BASE_URL = "https://channel.dasomi.ai/API/"
//        private const val BASE_URL = "https://dev.dasomi.ai/API/"
    var ApiLogger = HttpLoggingInterceptor.Logger { message -> Log.d(App.TAG, message) }

    private fun getRetrofit(): Retrofit {
        val interceptor = HttpLoggingInterceptor(ApiLogger)
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}