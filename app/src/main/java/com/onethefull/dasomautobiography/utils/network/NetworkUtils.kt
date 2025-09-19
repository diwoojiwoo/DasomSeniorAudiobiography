package com.onethefull.dasomautobiography.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * Created by Douner on 2020-01-15.
 */
object NetworkUtils {
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}

object NetworkStatusCode {
    const val SUCCESS = 0
    const val ERROR_ELDERLY_INFO_NOT_EXIST = 1001
    const val ERROR_ELDERLY_NOT_REGISTERED = -3
    const val ERROR_NETWORK = -1
    const val ERROR_AUTOBIOGRAPHY_QUESTION_NOT_EXIST = -97
    const val ERROR_INSERT_LOG_FAILED_SPECIFIC = -99
    const val ERROR_SOME_SPECIFIC_ISSUE_NEGATIVE_104 = -104
}