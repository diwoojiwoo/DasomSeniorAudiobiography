package com.onethefull.dasomautobiography.utils

import android.os.Build
import java.util.HashMap

/**
 * Created by Douner on 2019. 4. 12..
 */
object ParamGeneratorUtils {
    var SERIAL_NUMBER = Build.SERIAL

    fun getSerialnum(serialnum: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["serialNum"] = serialnum
        return params
    }

    fun getDiarySentenceParam(serialNumber: String, date: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["serialNum"] = serialNumber
        params["date"] = date
        return params
    }

    fun getDeleteLogParam(serialNum: String, autobiographyId: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["serialNum"] = serialNum
        params["autobiographyId"] = autobiographyId
        return params
    }
}