package com.onethefull.dasomautobiography.repository

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.data.api.ApiHelper
import com.onethefull.dasomautobiography.data.api.ApiHelperImpl
import com.onethefull.dasomautobiography.data.api.RetrofitBuilder
import com.onethefull.dasomautobiography.data.model.NotExistData
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyContentResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyMenuResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponseV2
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import com.onethefull.dasomautobiography.ui.diary.DiaryStatus

/**
 * Created by sjw on 2024. 12. 5.
 */
class SplashRepository(
    private val context: Context,
) {
    suspend fun check204(): Boolean {
        return apiHelper.check204()
    }

    suspend fun getContent(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
    ) : GetAutobiographyContentResponse{
        return  apiHelper.getContent(
            customerCode,
            deviceCode,
            serialNum
        )
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: SplashRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: SplashRepository(context).also { it.also { instance = it } }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}