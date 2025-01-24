package com.onethefull.dasomautobiography.repository

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.data.api.ApiHelper
import com.onethefull.dasomautobiography.data.api.ApiHelperImpl
import com.onethefull.dasomautobiography.data.api.RetrofitBuilder
import com.onethefull.dasomautobiography.data.model.NotExistData
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import com.onethefull.dasomautobiography.ui.diary.DiaryStatus

/**
 * Created by sjw on 2024. 12. 5.
 */
class DiaryRepository(
    private val context: Context,
) {
    suspend fun check204(): Boolean {
        return apiHelper.check204()
    }

    suspend fun getDiarySenetence(
        customerCode :String,
        deviceCode : String,
        serialNum : String,
        date : String,
        type : String
    ) : GetDiarySentenceResponse {
        return apiHelper.getDiarySentence(
            customerCode,
            deviceCode,
            serialNum,
            date,
            type
        )
    }

    fun getNotExistDiaryComment() : List<NotExistData> {
        val list = ArrayList<NotExistData>()
        list.add(
            NotExistData(
                DiaryStatus.NOT_EXIST_1,
                R.drawable.no_diary1,
                "일기장 기록이 없어요.",
                "다솜 기기가 꺼져있거나 바쁜 일이 있으셨나 봐요. \n저를 많이 사용해 주시면 내일 일기를 확인하실 수 있어요!",
                "일기장 기록이 없어요. 다솜 기기가 꺼져있거나 바쁜 일이 있으셨나 봐요. 저를 많이 사용해 주시면 내일 일기를 확인하실 수 있어요!"
            )
        )
        list.add(
            NotExistData(
                DiaryStatus.NOT_EXIST_2,
                R.drawable.no_diary2,
                "",
                "바쁜 일이 있으셨던 걸까요?...\n오늘 다솜이와 다양한 활동을 하시면, 다음 날 일기로 기록해 드려요!",
                "바쁜 일이 있으셨던 걸까요?...오늘 다솜이와 다양한 활동을 하시면, 다음 날 일기로 기록해 드려요!"
            )
        )
        list.add(
            NotExistData(
                DiaryStatus.NOT_EXIST_3,
                R.drawable.no_diary3,
                "",
                "어제 다솜이와 이야기를 못 해 아쉬웠어요! \n 오늘은 주인님과 하루를 같이 보내며 특별한 일기를 써보고 싶어요!",
                "어제 다솜이와 이야기를 못 해 아쉬웠어요! 오늘은 주인님과 하루를 같이 보내며 특별한 일기를 써보고 싶어요!"
            )
        )
        return list
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: DiaryRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: DiaryRepository(context).also { it.also { instance = it } }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}