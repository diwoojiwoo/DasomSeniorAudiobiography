package com.onethefull.dasomautobiography.data.api

import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse

/**
 * Created by sjw on 2021/11/10
 */
interface ApiHelper {
    suspend fun check204(): Boolean

    suspend fun getDiarySentence(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        date: String,
        type: String
    ) : GetDiarySentenceResponse
}