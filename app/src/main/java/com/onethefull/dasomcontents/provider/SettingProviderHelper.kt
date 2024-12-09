package com.onethefull.dasomcontents.provider

import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.net.Uri
import android.os.Parcelable
import com.onethefull.dasomcontents.App
import kotlinx.android.parcel.Parcelize

/**
 * Created by Douner on 2019-11-14.
 */
class SettingProviderHelper {
    private val context: Context? = null

    @Parcelize
    data class ProviderInsertData(
        val key: String,
        val value: String,
        val etc: String
    ) : Parcelable

    companion object {
        const val uriCode = 1
        var uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(WONDERFUL_PROVIDER_AUTH, WONDERFUL_SETTING, uriCode)
            addURI(WONDERFUL_PROVIDER_AUTH, "$WONDERFUL_SETTING/*", uriCode)
        }

        const val DATABASE_NAME = "wonderful_setttings_database"
        const val TABLE_NAME = "wonderful_setttings_database"
        const val DATABASE_VERSION = 1

        const val COLUMN_NAME_SETTING_KEY = "setting_key"
        const val COLUMN_NAME_SETTING_VALUE = "setting_value"
        const val COLUMN_NAME_SETTING_ETC = "setting_etc_value"

        private const val WONDERFUL_PROVIDER_AUTH = "com.onethefull.database.provider"
        private const val WONDERFUL_SETTING = "settings"
        private val BASE_URI: Uri = Uri.parse("content://$WONDERFUL_PROVIDER_AUTH")
        val BASE_PATH_URI: Uri = BASE_URI.buildUpon().appendPath(WONDERFUL_SETTING).build()

        fun insert(insertData: ProviderInsertData) {
            val values = ContentValues()
            values.put(COLUMN_NAME_SETTING_KEY, insertData.key)
            values.put(COLUMN_NAME_SETTING_VALUE, insertData.value)
            values.put(COLUMN_NAME_SETTING_ETC, insertData.etc)

            App.instance.contentResolver.insert(
                BASE_PATH_URI,
                values
            )
        }

        fun getEtc(context: Context?, searchKey: String?): String? {
            try {
                val columns = arrayOf(
                    COLUMN_NAME_SETTING_ETC,
                    COLUMN_NAME_SETTING_KEY,
                    COLUMN_NAME_SETTING_VALUE
                )
                val cursor = context?.contentResolver?.query(
                    BASE_PATH_URI, columns,
                    "${COLUMN_NAME_SETTING_KEY}=?",
                    arrayOf(searchKey),
                    null
                )
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val key = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_KEY))
                        val value = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_VALUE))
                        val etc = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_ETC))
                        if (key == searchKey) {
                            return etc
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun get(context: Context?, searchKey: String?): String? {
            try {
                val columns = arrayOf(
                    COLUMN_NAME_SETTING_ETC,
                    COLUMN_NAME_SETTING_KEY,
                    COLUMN_NAME_SETTING_VALUE
                )
                val cursor = context?.contentResolver?.query(
                    BASE_PATH_URI, columns,
                    "${COLUMN_NAME_SETTING_KEY}=?",
                    arrayOf(searchKey),
                    null
                )
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val key = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_KEY))
                        val value = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_VALUE))
                        val etc = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SETTING_ETC))
                        if (key == searchKey) {
                            return value
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}