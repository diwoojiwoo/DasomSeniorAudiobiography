package com.onethefull.dasomautobiography.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View

/**
 * Created by sjw on 2025. 6. 4.
 */

private var lastClickTimeMap = mutableMapOf<Int, Long>()


fun View.setOnSingleClickListener(delay: Long = 3000L, onClick: (View) -> Unit) {
    this.setOnClickListener {
        val viewId = this.id
        val currentTime = System.currentTimeMillis()
        val lastClickTime = lastClickTimeMap[viewId] ?: 0L

        if (currentTime - lastClickTime < delay) {
            // 중복 클릭 방지
            return@setOnClickListener
        }

        lastClickTimeMap[viewId] = currentTime
        onClick(this)
    }
}

fun Int.dpToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}
