package com.onethefull.dasomautobiography.data.api
/**
 * Created by Douner on 2019. 4. 12..
 */
class BaseResponseCallback {
    interface ErrorListener {
        fun onErrorResponse(e: Throwable)
    }

    interface Listener<T> {
        fun onResponse(value: T)
    }
}
