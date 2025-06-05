package com.onethefull.dasomautobiography.utils

import android.view.View

/**
 * Created by sjw on 2025. 6. 4.
 */

fun View.setOnSingleClickListener(interval: Long = 5000L, listener: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            listener(it)
        }
    }
}
