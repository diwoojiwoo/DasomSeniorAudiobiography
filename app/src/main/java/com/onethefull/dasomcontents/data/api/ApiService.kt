package com.onethefull.dasomcontents.data.api
import com.onethefull.dasomcontents.data.model.memory.InsertMemoryQuizLogRequest
import com.onethefull.dasomcontents.data.model.memory.InsertMemoryQuizLogResponse
import com.onethefull.dasomcontents.data.model.memory.SelectMemoryQuizResponse
import retrofit2.http.*
import retrofit2.http.Body

/**
 * Created by sjw on 2021/11/10
 */
interface ApiService {
    /**
     * 추억소환
     * */
    // 추억소환 문제 제출(로봇)
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/dementia/selectMemoryQuiz")
    suspend fun selectMemoryQuiz(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: Map<String, String>,
    ): SelectMemoryQuizResponse


    // 추억소환 문제 로그 저장(로봇)
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/dementia/insertMemoryQuizLog")
    suspend fun insertMemoryQuizLog(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: InsertMemoryQuizLogRequest,
    ): InsertMemoryQuizLogResponse
}