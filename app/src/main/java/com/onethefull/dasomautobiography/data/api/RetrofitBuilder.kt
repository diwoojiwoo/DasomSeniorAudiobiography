package com.onethefull.dasomautobiography.data.api

import android.annotation.SuppressLint
import android.util.Log
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.data.api.network.TLSSocketFactory
import com.onethefull.dasomautobiography.data.api.service.ApiService
import com.onethefull.wonderfulrobotmodule.ext.dasomLangValue
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
    // 기본 Dasom API
    private const val DASOM_BASE_URL = "https://channel.dasomi.ai/API/"
    // private const val BASE_URL = "https://dev.dasomi.ai/API/"

    private val apiLogger = HttpLoggingInterceptor.Logger { message -> Log.d(App.TAG, message) }

    @SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
    private fun <T> createApiService(service: Class<T>, baseUrl: String): T {
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor(apiLogger).setLevel(HttpLoggingInterceptor.Level.BODY))
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
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("device", "dasom")
                        .addHeader("lang", App.instance.getLocale()?.dasomLangValue() ?: "ko")
                        .build()
                )
            }
        }.build()

        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl) // 반드시 끝에 / 있어야 함
            .build()
            .create(service)
    }

    // var로 선언해서 changeHost()에서 재할당 가능
    var apiService: ApiService = createApiService(ApiService::class.java, DASOM_BASE_URL)

    /**
     * host를 받아서 apiService 재생성
     */
    private fun createService(host: String) {
        if (host.isNotEmpty()) {
            // host 끝에 / 없으면 제거 → "/API/" 붙이기 → 항상 /로 끝나도록
            val normalizedHost = host.trimEnd('/')
            val baseUrl = "$normalizedHost/API/"

            Log.d("HostTestTag", "changeHost -> $baseUrl")

            apiService = createApiService(ApiService::class.java, baseUrl)
        } else {
            // 비어있으면 기본값 사용
            Log.w("HostTestTag", "createService: host is empty, using default DASOM_BASE_URL")
            apiService = createApiService(ApiService::class.java, DASOM_BASE_URL)
        }
    }

    /**
     * provider에서 host를 가져와 apiService 변경
     */
    fun changeHost() {
        val host = App.instance.provider.getHostUrl()
        createService(host)
    }

    /**
     * 초기화 시점에 provider host로 apiService 설정
     */
    init {
        changeHost()
    }
}