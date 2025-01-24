package com.onethefull.dasomautobiography.provider

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Parcelable
import com.onethefull.dasomautobiography.utils.logger.DWLog
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import java.lang.Exception
import java.lang.StringBuilder

/**
 * Created by Douner on 2019-11-26.
 *
 * DasomProviderHelper VERSION_1.0.0
 *
 */

@Suppress("MemberVisibilityCanBePrivate")
object DasomProviderHelper {

    const val uriCode = 1
    var uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(WONDERFUL_PROVIDER_AUTH, WONDERFUL_SETTING, uriCode)
        addURI(WONDERFUL_PROVIDER_AUTH, "$WONDERFUL_SETTING/*", uriCode)
    }

    const val DATABASE_NAME = "wonderful_settings_database"
    const val TABLE_NAME = "wonderful_settings_database"
    const val DATABASE_VERSION = 1

    const val COLUMN_NAME_SETTING_KEY = "setting_key"
    const val COLUMN_NAME_SETTING_VALUE = "setting_value"
    const val COLUMN_NAME_SETTING_ETC = "setting_etc_value"

    // 다솜 서비스 URL

    const val SETTING_ETC_EMPTY = "0"

    private const val WONDERFUL_PROVIDER_AUTH = "com.onethefull.database.global.provider"
    private const val WONDERFUL_SETTING = "settings"
    private val BASE_URI: Uri = Uri.parse("content://$WONDERFUL_PROVIDER_AUTH")
    val BASE_PATH_URI: Uri = BASE_URI.buildUpon().appendPath(WONDERFUL_SETTING).build()


    const val KEY_GLOBAL_TYPE = "key_global_type"

    // GLOBAL PARAMS
    const val KEY_GLOBAL_DEVICE_CODE = "key_global_device_code"
    const val KEY_GLOBAL_CUSTOMER_CODE = "key_global_customer_code"
    const val KEY_GLOBAL_SERIAL_NUMBER_CODE = "key_global_serialnumber_code"

    // MQTT URL
    const val KEY_GLOBAL_MQTT_URL = "key_global_mqtt_url"

    // QUICK BLOX
    const val KEY_QB_APPLICATION_ID = "key_qb_application_id"
    const val KEY_QB_SECRET_KEY = "key_qb_secret_key"
    const val KEY_QB_AUTH_KEY = "key_qb_auth_key"

    // innerTtsVersionCode
    const val KEY_COMMUNITY_JOIN = "key_community_join"
    const val KEY_COMMUNITY_MAIN = "key_community_main"
    const val KEY_COMMUNITY_MAIN_VALUE = "key_community_main_value"

    const val KEY_COMMUNITY_RECOMMEND = "key_community_recommend"
    const val KEY_COMMUNITY_RECOMMEND_VALUE = "key_community_recommend_value"

    const val KEY_COMMUNITY_USER = "key_community_user"
    const val KEY_COMMUNITY_USER_VALUE = "key_community_user_value"

    // practice_emergency
    const val KEY_PRACTICE_EMERGENCY = "key_practice_emergency"
    const val KEY_PRACTICE_EMERGENCY_VALUE = "key_practice_emergency_value"

    const val KEY_PRACTICE_EMERGENCY_COMPLETE = "key_practice_emergency_complete"
    const val KEY_PRACTICE_EMERGENCY_COMPLETE_VALUE = "key_practice_emergency_complete_value"

    const val KEY_PRACTICE_EMERGENCY_END = "key_practice_emergency_end"
    const val KEY_PRACTICE_EMERGENCY_END_VALUE = "key_practice_emergency_end_value"

    const val KEY_PRACTICE_EMERGENCY_HALF = "key_practice_emergency_half"
    const val KEY_PRACTICE_EMERGENCY_HALF_VALUE = "key_practice_emergency_half_value"

    const val KEY_PRACTICE_EMERGENCY_RETRY = "key_practice_emergency_retry"
    const val KEY_PRACTICE_EMERGENCY_RETRY_VALUE = "key_practice_emergency_retry_value"

    const val KEY_PRACTICE_EMERGENCY_START = "key_practice_emergency_start"
    const val KEY_PRACTICE_EMERGENCY_START_VALUE = "key_practice_emergency_start_value"

    const val KEY_PRACTICE_EMERGENCY_TASK_COUNT = "key_practice_emergency_task_count"
    const val KEY_PRACTICE_EMERGENCY_NO_RESPONSE_COUNT = "key_practice_emergency_no_response_count"

    // 얼굴 체크
    const val KEY_FACE_DETECTION_ID = "key_face_detected_id"

    // TopScene
    const val KEY_TOP_SCENE = "provider_top_scene"
    const val VALUE_EMPTY = "empty_"

    const val KEY_SYSTEM_LANGUAGE = "key_system_language" // 시스템언어

    /**
     * Insert Data
     */
    fun insert(context: Context, insertData: ProviderInsertData) {
        val values = ContentValues()
        try {
            values.put(COLUMN_NAME_SETTING_ETC, insertData.etc)
            values.put(COLUMN_NAME_SETTING_KEY, insertData.key)
            values.put(COLUMN_NAME_SETTING_VALUE, insertData.value)
            val result = context.contentResolver.insert(BASE_PATH_URI, values)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    @SuppressLint("Recycle")
    fun selectMqttUrl(context: Context): String {
        val columns = arrayOf(
            COLUMN_NAME_SETTING_ETC,
            COLUMN_NAME_SETTING_KEY,
            COLUMN_NAME_SETTING_VALUE
        )
        val cursor = context.contentResolver.query(
            BASE_PATH_URI, columns,
            "${COLUMN_NAME_SETTING_KEY}=?",
            arrayOf(KEY_GLOBAL_MQTT_URL),
            null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_VALUE))
            }
        }
        return ""
    }

    /**
     * @param context application context
     * @param key UrlProviderHelper local key
     */
    @SuppressLint("Recycle")
    fun selectTypeData(context: Context?, key: String): String {
        val strBuilder = StringBuilder()
        val columns = arrayOf(
            COLUMN_NAME_SETTING_ETC,
            COLUMN_NAME_SETTING_KEY,
            COLUMN_NAME_SETTING_VALUE
        )
        val cursor = context?.contentResolver?.query(
            BASE_PATH_URI, columns,
            "${COLUMN_NAME_SETTING_KEY}=?",
            arrayOf(key),
            null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                strBuilder.append(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_VALUE)))
            }
        }
        cursor?.close()
        DWLog.d("${context?.applicationInfo?.packageName} : select[$key]  value[$strBuilder]")
        return strBuilder.toString()
    }

    fun getCustomerCode(context: Context?): String {
        return selectTypeData(context, KEY_GLOBAL_CUSTOMER_CODE)
    }

    fun getDeviceCode(context: Context?): String {
        return selectTypeData(context, KEY_GLOBAL_DEVICE_CODE)
    }

    fun getQBSecretKey(context: Context?): String {
        return selectTypeData(context, KEY_QB_SECRET_KEY)
    }

    fun getQBAuthKey(context: Context?): String {
        return selectTypeData(context, KEY_QB_AUTH_KEY)
    }

    fun getQBApplicationId(context: Context?): String {
        return selectTypeData(context, KEY_QB_APPLICATION_ID)
    }

    fun getInnerCommMainTtsVersion(context: Context?): String {
        selectTypeData(context, KEY_COMMUNITY_MAIN).run {
            return if (this.isEmpty()) "0" else this
        }
    }

    fun getJoinCommCount(context: Context?): String {
        selectTypeData(context, KEY_COMMUNITY_JOIN).run {
            return if (this.isEmpty()) "0" else this
        }
    }

    fun getInnerCommMainTtsList(context: Context?): String {
        try {
            return selectTypeData(context, KEY_COMMUNITY_MAIN_VALUE)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun getPracticeEmergencyValue(context: Context, key: String): String {
        selectTypeData(context, key).run {
            return if (this.isEmpty()) "" else this
        }
    }

    fun getPracticeEmergencyTaskCnt(context: Context): String {
        selectTypeData(context, KEY_PRACTICE_EMERGENCY_TASK_COUNT).run {
            return if (this.isEmpty()) "" else this
        }
    }

    fun getPracticeEmergencyNoResponseCnt(context: Context): String {
        selectTypeData(context, KEY_PRACTICE_EMERGENCY_NO_RESPONSE_COUNT).run {
            return if (this.isEmpty()) "" else this
        }
    }

    fun getFaceDetectId(context: Context?): String {
        selectTypeData(
            context,
            KEY_FACE_DETECTION_ID
        ).run {
            return if (this.isNullOrEmpty()) "0" else this
        }
    }

    // Android 10 버전 이상
    fun getSerialNumber(context: Context): String {
        return selectTypeData(context, KEY_GLOBAL_SERIAL_NUMBER_CODE)
    }

    fun isIgnoreScene(context: Context?): Boolean {
        try {
            getTopScene(context, KEY_TOP_SCENE).run {
                return this == "LAUNCHER"
//                    return this == "DASOMI_SOODA" || this == "9SOS" || this == "Youtube" || this == "DASOMI_VIDEO_CALL" || this == "WDAlarm"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun getTopScene(context: Context?, searchKey: String?): String {
        val columns = arrayOf(
            COLUMN_NAME_SETTING_ETC,
            COLUMN_NAME_SETTING_KEY,
            COLUMN_NAME_SETTING_VALUE
        )
        val cursor: Cursor? = context?.contentResolver?.query(
            BASE_PATH_URI,
            columns,
            "${COLUMN_NAME_SETTING_KEY}=?", arrayOf(searchKey),
            null
        )
        cursor.use { cursor ->
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val key = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_KEY))
                    val value =
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_VALUE))
                    val etc = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_ETC))
                    if (key == searchKey) {
                        return value
                    }
                }
            }
        }
        return "Empty"
    }

    @Parcelize
    data class ProviderInsertData(
        val key: String,
        val value: String,
        val etc: String,
    ) : Parcelable
}