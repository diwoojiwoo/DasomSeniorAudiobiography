package com.onethefull.dasomcontents.data.api

import android.annotation.SuppressLint
import android.os.Build
import com.onethefull.dasomcontents.App
import com.onethefull.dasomcontents.BuildConfig
import com.onethefull.dasomcontents.data.model.*
import com.onethefull.dasomcontents.data.model.memory.InsertMemoryQuizLogRequest
import com.onethefull.dasomcontents.data.model.memory.InsertMemoryQuizLogResponse
import com.onethefull.dasomcontents.data.model.memory.SelectMemoryQuizResponse
import com.onethefull.dasomcontents.utils.ParamGeneratorUtils
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

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

    override suspend fun selectMemoryQuiz(
        customerCode: String,
        deviceCode: String,
        serialNum: String
    ): SelectMemoryQuizResponse = apiService.selectMemoryQuiz(
        customerCode,
        deviceCode,
        ParamGeneratorUtils.getSerialnum(serialNum)
    )

    override suspend fun insertMemoryQuizLog(
        customerCode: String,
        deviceCode: String,
        request: InsertMemoryQuizLogRequest
    ): InsertMemoryQuizLogResponse = apiService.insertMemoryQuizLog(
        customerCode,
        deviceCode,
        request
    )

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