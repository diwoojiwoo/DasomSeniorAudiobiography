package com.onethefull.dasomcontents.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.onethefull.dasomcontents.BuildConfig
import com.onethefull.dasomcontents.data.api.ApiHelper
import com.onethefull.dasomcontents.data.api.ApiHelperImpl
import com.onethefull.dasomcontents.data.api.RetrofitBuilder
import com.onethefull.dasomcontents.data.model.memory.SelectMemoryQuizResponse
import com.onethefull.dasomcontents.provider.DasomProviderHelper

/**
 * Created by sjw on 2024. 12. 5.
 */
class MemoryContentRepository(
    private val context: Context,
) {
    suspend fun check204(): Boolean {
        return apiHelper.check204()
    }

    suspend fun selectMemoryQuiz(): SelectMemoryQuizResponse {
        return apiHelper.selectMemoryQuiz(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            Build.SERIAL
        )
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MemoryContentRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: MemoryContentRepository(context).also { it.also { instance = it } }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}