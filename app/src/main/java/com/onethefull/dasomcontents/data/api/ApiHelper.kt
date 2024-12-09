package com.onethefull.dasomcontents.data.api

import com.onethefull.dasomcontents.data.model.*
import com.onethefull.dasomcontents.data.model.memory.InsertMemoryQuizLogRequest
import com.onethefull.dasomcontents.data.model.memory.InsertMemoryQuizLogResponse
import com.onethefull.dasomcontents.data.model.memory.SelectMemoryQuizResponse

/**
 * Created by sjw on 2021/11/10
 */
interface ApiHelper {
    suspend fun check204(): Boolean

    suspend fun selectMemoryQuiz (
        customerCode: String,
        deviceCode: String,
        serialNum: String
    ) : SelectMemoryQuizResponse

    suspend fun insertMemoryQuizLog(
        customerCode: String,
        deviceCode: String,
        request : InsertMemoryQuizLogRequest
    ) : InsertMemoryQuizLogResponse

}