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
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import com.onethefull.dasomautobiography.ui.diary.DiaryStatus

/**
 * Created by sjw on 2024. 12. 5.
 */
class MenuRepository(
    private val context: Context,
) {
    suspend fun check204(): Boolean {
        return apiHelper.check204()
    }


    suspend fun getCategoryList(
        customerCode: String,
        deviceCode: String,
        serialNum: String
    ): GetCategoryListResponse {
        return apiHelper.getCategoryList(
            customerCode,
            deviceCode,
            serialNum
        )
    }

    suspend fun getQuestionList(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        type : String
    ) : GetAutobiographyMenuResponse {
        return  apiHelper.getQuestionList(
            customerCode,
            deviceCode,
            serialNum,
            type
        )
    }

    suspend fun getDiarySenetence(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        date: String,
        type: String
    ): GetDiarySentenceResponse {
        return apiHelper.getDiarySentence(
            customerCode,
            deviceCode,
            serialNum,
            date,
            type
        )
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MenuRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: MenuRepository(context).also { it.also { instance = it } }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}