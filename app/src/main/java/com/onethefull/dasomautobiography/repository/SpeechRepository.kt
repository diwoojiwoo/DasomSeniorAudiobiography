package com.onethefull.dasomautobiography.repository

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.data.api.ApiHelper
import com.onethefull.dasomautobiography.data.api.ApiHelperImpl
import com.onethefull.dasomautobiography.data.api.RetrofitBuilder
import com.onethefull.dasomautobiography.data.model.NotExistData
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyMenuResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.InsertLogResponse
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import com.onethefull.dasomautobiography.repository.MenuRepository.Companion
import com.onethefull.dasomautobiography.ui.diary.DiaryStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by sjw on 2024. 12. 5.
 */
class SpeechRepository(
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


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: SpeechRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: SpeechRepository(context).also { it.also { instance = it } }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}