package com.onethefull.dasomautobiography.data.api.service
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import retrofit2.http.*

/**
 * Created by sjw on 2021/11/10
 */
interface ApiService {
    /**
     * 일기장
     * */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/alarm/getDiarySentence/{type}")
    suspend fun getDiarySentence(
        @Header("languageCode") languageCode: String,
        @Header("serviceCode") serviceCode: String,
        @Header("voiceCode") voiceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("type") type: String,
        @QueryMap params: Map<String, String>
    ) : GetDiarySentenceResponse
}