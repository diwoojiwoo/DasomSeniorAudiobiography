package com.onethefull.dasomautobiography.data.api.service

import com.onethefull.dasomautobiography.data.model.audiobiography.DeleteLogResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyContentResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyLogDtlResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyLogDtlResponseV2
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyMenuResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyMenuResponseV2
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponseV2
import com.onethefull.dasomautobiography.data.model.audiobiography.InsertLogResponse
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Header("serviceCode") serviceCode: String,
        @Header("voiceCode") voiceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("type") type: String,
        @QueryMap params: Map<String, String>
    ) : GetDiarySentenceResponse

    /**
     * 메인 화면 카테고리 Summary
     * */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/categoryList/{serialNum}")
    suspend fun getCategoryList(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("serialNum") serialNum: String,
    ): GetCategoryListResponse


    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/categoryList_v2/{serialNum}")
    suspend fun getCategoryListV2(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("serialNum") serialNum: String,
    ): GetCategoryListResponseV2

    /**
     * 자서전 전체 목록 리스트 호출
     * */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/list/{serialNum}/{type}")
    suspend fun getQuestionList(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("serialNum") serialNum: String,
        @Path("type") type: String,
    ): GetAutobiographyMenuResponse


    /**
     * 자서전 전체 목록 리스트 호출 V2
     * */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/list_v2/{serialNum}/{type}")
    suspend fun getQuestionListV2(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("serialNum") serialNum: String,
        @Path("type") type: String,
    ): GetAutobiographyMenuResponseV2

    /**
     * 자서전 로그 저장
     * */
    @Multipart
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/insertLog")
    suspend fun insertLog(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Part("serialNum") serialNum: RequestBody,
        @Part("autobiographyId") autobiographyId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("answerYn") answerYn: RequestBody,
        @Part file: MultipartBody.Part,
    ): InsertLogResponse


    /**
     * 자서전 로그 삭제
     * */
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/deleteLog")
    suspend fun deleteLog(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: Map<String, String>
    ): DeleteLogResponse

    /**
     * 자서전 발화 내용 호출
     * */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/list/{serialNum}")
    suspend fun getContent(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("serialNum") serialNum: String,
    ): GetAutobiographyContentResponse


    /**
     * 자서전 로그 상세 보기
     * */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/dtl/{serialNum}/{logId}")
    suspend fun getLogDtl(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("serialNum") serialNum: String,
        @Path("logId") logId: String,
    ): GetAutobiographyLogDtlResponse


    /**
     * 자서전 로그 상세 보기
     * autobiographyId 통하여 해당 질문에 대한 전체 답변 가져옴
     * */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/autobiography/dtl_v2/{serialNum}/{logId}")
    suspend fun getLogDtlV2(
        @Header("lang") lang: String,
        @Header("languageCode") languageCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("serialNum") serialNum: String,
        @Path("logId") logId: String,
    ): GetAutobiographyLogDtlResponseV2
}