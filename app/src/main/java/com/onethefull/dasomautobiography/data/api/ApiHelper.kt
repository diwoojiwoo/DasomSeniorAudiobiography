package com.onethefull.dasomautobiography.data.api

import com.onethefull.dasomautobiography.data.model.audiobiography.DeleteLogResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyLogDtlResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyMenuResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyMenuResponseV2
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponseV2
import com.onethefull.dasomautobiography.data.model.audiobiography.InsertLogResponse
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by sjw on 2021/11/10
 */
interface ApiHelper {
    suspend fun check204(): Boolean

    suspend fun getCategoryList(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
    ): GetCategoryListResponse

    suspend fun getCategoryListV2(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
    ): GetCategoryListResponseV2

    suspend fun getQuestionList(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        type: String
    ): GetAutobiographyMenuResponse

    suspend fun getQuestionListV2(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        type: String
    ): GetAutobiographyMenuResponseV2

    suspend fun getDiarySentence(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        date: String,
        type: String
    ): GetDiarySentenceResponse

    suspend fun insertLog(
        customerCode: String,
        deviceCode: String,
        serialNum: RequestBody,
        autobiographyId: RequestBody,
        type: RequestBody,
        answerYN: RequestBody,
        file: MultipartBody.Part
    ): InsertLogResponse

    suspend fun deleteLog(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        autobiographyId: String
    ): DeleteLogResponse

    suspend fun getLogDtl(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        autobiographyId : String
    ) : GetAutobiographyLogDtlResponse
}