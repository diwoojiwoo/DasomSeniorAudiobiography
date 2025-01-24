package com.onethefull.dasomautobiography.utils

import java.util.HashMap

/**
 * Created by Douner on 2019. 4. 12..
 */
object ParamGeneratorUtils {
    fun getSerialnum(serialnum: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["serialNum"] = serialnum
        return params
    }

    fun getDiarySentenceParam(serialNumber: String, date : String) : HashMap<String, String> {
        val params = HashMap<String, String>()
        params["serialNum"] = serialNumber
        params["date"] = date
        return params
    }
}
