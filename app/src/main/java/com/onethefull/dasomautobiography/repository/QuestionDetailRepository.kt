package com.onethefull.dasomautobiography.repository

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomautobiography.data.api.ApiHelper
import com.onethefull.dasomautobiography.data.api.ApiHelperImpl
import com.onethefull.dasomautobiography.data.api.RetrofitBuilder
import com.onethefull.dasomautobiography.data.model.audiobiography.DeleteLogResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyLogDtlResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.InsertLogResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by sjw on 2024. 12. 5.
 */
class QuestionDetailRepository(
    private val context: Context,
) {
    suspend fun check204(): Boolean {
        return apiHelper.check204()
    }

    suspend fun insertLog(
        customerCode: String,
        deviceCode: String,
        serialNum: RequestBody,
        autobiographyId: RequestBody,
        type: RequestBody,
        answerYN: RequestBody,
        file: MultipartBody.Part
    ): InsertLogResponse {
        return apiHelper.insertLog(
            customerCode,
            deviceCode,
            serialNum,
            autobiographyId,
            type,
            answerYN,
            file
        )
    }

    suspend fun deleteLog(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        autobiographyId : String
    ) :DeleteLogResponse {
        return apiHelper.deleteLog(
            customerCode,
            deviceCode,
            serialNum,
            autobiographyId
        )
    }

    suspend fun getLogDtl(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        autobiographyId : String
    ) : GetAutobiographyLogDtlResponse {
        return apiHelper.getLogDtl(
            customerCode,
            deviceCode,
            serialNum,
            autobiographyId
        )
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: QuestionDetailRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: QuestionDetailRepository(context).also { it.also { instance = it } }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}