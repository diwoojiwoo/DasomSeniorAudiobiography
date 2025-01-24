package com.onethefull.dasomautobiography.data.api

import android.annotation.SuppressLint
import android.util.Log
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.data.api.network.TLSSocketFactory
import com.onethefull.dasomautobiography.data.api.service.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

/**
 * Created by sjw on 2021/11/10
 */

object RetrofitBuilder {
    // Dasom API 개발
    private const val DASOM_BASE_URL = "https://channel.dasomi.ai/API/"
    // private const val BASE_URL = "https://dev.dasomi.ai/API/"

    private var ApiLogger = HttpLoggingInterceptor.Logger { message -> Log.d(App.TAG, message) }

    @SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
    fun <T> createApiService(service: Class<T>, baseUrl: String): T {
        val okhttpBuilder = OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor(ApiLogger).setLevel(HttpLoggingInterceptor.Level.BODY))
            sslSocketFactory(TLSSocketFactory(), object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?,
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?,
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }
            })
            connectTimeout(35L, TimeUnit.SECONDS)
            readTimeout(35L, TimeUnit.SECONDS)
            writeTimeout(35L, TimeUnit.SECONDS)
            addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().addHeader("device", "dasom").build())
            }
        }

        val retrofit = Retrofit.Builder()
            .client(okhttpBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl)
            .build()
        return retrofit.create(service)
    }

    val apiService: ApiService = createApiService(ApiService::class.java, DASOM_BASE_URL)
}
