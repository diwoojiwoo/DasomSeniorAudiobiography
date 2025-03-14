package com.onethefull.dasomautobiography.data.api

import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.data.api.service.ApiService
import com.onethefull.dasomautobiography.data.model.diary.GetDiarySentenceResponse
import com.onethefull.dasomautobiography.utils.ParamGeneratorUtils
import com.onethefull.dasomautobiography.BuildConfig
import com.onethefull.dasomautobiography.data.model.audiobiography.DeleteLogResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyLogDtlResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyMenuResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.GetCategoryListResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.InsertLogResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Created by sjw on 2021/11/10
 */
class ApiHelperImpl(private val apiService: ApiService) : ApiHelper {
    override suspend fun check204(): Boolean {
        val cc = CheckConnection("http://clients3.google.com/generate_204")
        return try {
            cc.start()
            cc.join()
            cc.isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getCategoryList(
        customerCode: String,
        deviceCode: String,
        serialNum: String
    ): GetCategoryListResponse = apiService.getCategoryList(
        customerCode,
        deviceCode,
        serialNum
    )

    override suspend fun getQuestionList(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        type: String
    ): GetAutobiographyMenuResponse = apiService.getQuestionList(
        customerCode,
        deviceCode,
        serialNum,
        type
    )

    override suspend fun insertLog(
        customerCode: String,
        deviceCode: String,
        serialNum: RequestBody,
        autobiographyId: RequestBody,
        type: RequestBody,
        answerYN: RequestBody,
        file: MultipartBody.Part
    ): InsertLogResponse = apiService.insertLog(
        customerCode,
        deviceCode,
        serialNum,
        autobiographyId,
        type,
        answerYN,
        file
    )

    override suspend fun deleteLog(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        autobiographyId: String
    ): DeleteLogResponse = apiService.deleteLog(
        customerCode,
        deviceCode,
        ParamGeneratorUtils.getDeleteLogParam(serialNum, autobiographyId)
    )

    override suspend fun getLogDtl(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        logId: String
    ): GetAutobiographyLogDtlResponse = apiService.getLogDtl(
        customerCode,
        deviceCode,
        serialNum,
        logId
    )

    override suspend fun getDiarySentence(
        customerCode: String,
        deviceCode: String,
        serialNum: String,
        date: String,
        type: String
    ): GetDiarySentenceResponse = apiService.getDiarySentence(
        languageCode = getLocale(),
        serviceCode = getServiceCode(),
        voiceCode = getVoiceCode(),
        customerCode,
        deviceCode,
        type,
        ParamGeneratorUtils.getDiarySentenceParam(serialNum, date)
    )

    private fun getLocale(): String {
        App.instance.defaultLanguage?.let {
            return when (it.language) {
                Locale.KOREA.language -> "ko-KR"
                Locale.US.language -> "en-US"
//                Locale.JAPAN.language -> "ja-JP"
//                Locale.CHINA.language -> "zh-CN"
                else -> getHardcodingLocale()
            }
        }
        return getHardcodingLocale()
    }

    fun getServiceCode(): String {
        App.instance.defaultLanguage?.let {
            return when (it.language) {
                Locale.KOREA.language -> if (BuildConfig.CHARACTER_TYPE != "NewDasom") "naver" else "google"
                Locale.US.language -> "aws"
                else -> if (BuildConfig.CHARACTER_TYPE != "NewDasom") "naver" else "google"
            }
        }
        return if (BuildConfig.CHARACTER_TYPE != "NewDasom") "naver" else "google"
    }

    fun getVoiceCode(): String {
        App.instance.defaultLanguage?.let {
            return when (it.language) {
                Locale.KOREA.language -> if (BuildConfig.CHARACTER_TYPE != "NewDasom") "nara" else "ko-KR-Neural2-A"
                Locale.US.language -> "Ivy"
                else -> if (BuildConfig.CHARACTER_TYPE != "NewDasom") "nara" else "ko-KR-Neural2-A"
            }
        }
        return if (BuildConfig.CHARACTER_TYPE != "NewDasom") "nara" else "ko-KR-Neural2-A"
    }

    /**
     * 언어 설정에 따른 헤더 언어 코드
     *
     * @return 헤더 언어 코드
     */
    private fun getHardcodingLocale(): String {
        return when (BuildConfig.LANGUAGE_TYPE) { // ko-KR(default), en-US
            "EN" -> "en-US"
            else -> "ko-KR"
        }
    }

    internal inner class CheckConnection(private val host: String) : Thread() {
        var isSuccess = false
        override fun run() {
            var urlConnection: HttpURLConnection? = null
            try {
                sleep(500)
                urlConnection = URL(host).openConnection() as HttpURLConnection
                urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"))
                urlConnection.connectTimeout = 1000
                urlConnection.connect()
                val responseCode = urlConnection.responseCode
                if (responseCode == 204) {
                    isSuccess = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            urlConnection?.disconnect()
        }
    }
}