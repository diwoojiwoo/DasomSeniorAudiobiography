package com.onethefull.dasomautobiography.utils.logger

import android.util.Log
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.BuildConfig

object DWLog {

    val debug = BuildConfig.DISPLAY_LOG

    fun i(message: String) {
        if (debug) i(App.TAG, "[${Thread.currentThread().name}] - $message")
    }

    fun i(TAG: String, message: String) {
        if (debug) Log.i(TAG, message)
    }

    fun w(message: String) {
        if (debug) w(App.TAG, "[${Thread.currentThread().name}] - $message")
    }

    fun w(TAG: String, message: String) {
        if (debug) Log.w(TAG, message)
    }

    fun d(message: String) {
        if (debug) d(App.TAG, "[${Thread.currentThread().name}] - $message")
    }

    fun d(TAG: String, message: String) {
        if (debug) Log.d(TAG, message)
    }

    fun v(message: String) {
        if (debug) d(App.TAG, "[${Thread.currentThread().name}] - $message")
    }

    fun v(TAG: String, message: String) {
        if (debug) Log.v(TAG, message)
    }


    fun e(message: String) {
        if (debug) Log.e(App.TAG, "[${Thread.currentThread().name}] - $message")
    }

    fun e(TAG: String, message: String) {
        if (debug) Log.e(TAG, message)
    }
}

