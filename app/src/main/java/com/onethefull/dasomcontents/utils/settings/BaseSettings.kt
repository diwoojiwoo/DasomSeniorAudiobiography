package com.onethefull.dasomcontents.utils.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.onethefull.dasomcontents.App
import com.onethefull.dasomcontents.provider.DasomProviderHelper
import java.util.Locale

/**
 * Created by Douner on 1/4/24.
 */
object BaseSettings {
    fun getSystemLocale(applicationContext: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationContext.resources.configuration.locales.get(0)
        } else {
            applicationContext.resources.configuration.locale
        }.apply {
            val language = this.language
            val country = this.country
            DasomProviderHelper.insert(
                App.instance, DasomProviderHelper.ProviderInsertData(
                    DasomProviderHelper.KEY_SYSTEM_LANGUAGE,
                    "$language-$country",
                    "empty"
                )
            )
        }
    }


    private fun getOrionStarSerial(): String {
        var serialNum: String
        if (Build.VERSION.SDK_INT < 26) {
            serialNum = getSystemProperties("ro.serialno.robot", "")
            if (TextUtils.isEmpty(serialNum) || serialNum.startsWith("Error")) {
                serialNum = getSystemProperties("ro.serialno", "")
            }
        } else {
            serialNum = getSystemProperties("ro.robot.serialno", "")
            if (TextUtils.isEmpty(serialNum) || serialNum.startsWith("Error")) {
                serialNum = Build.SERIAL
            }
        }
        return serialNum
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperties(key: String, defaultVal: String): String {
        try {
            val systemPropertiesGet =
                Class.forName("android.os.SystemProperties").getMethod("get", String::class.java)
            val ret: String = systemPropertiesGet.invoke(null, key) as String
            return if (TextUtils.isEmpty(ret)) defaultVal else ret
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defaultVal
    }

}