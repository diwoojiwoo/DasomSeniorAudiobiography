package com.onethefull.dasomautobiography.utils.bus

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Douner on 2019. 5. 8..
 */
class RxEvent {
    @Parcelize
    data class Event(
        val typeNumber: Int,
        val time: Long,
        val name: String,
    ) : Parcelable

    companion object {
        const val AppDestroy = 0x00
        const val AppDestroyUpdate = 0x01
        const val NavigateToMenuFragment = 0x02
        const val RemoveNavigateToMenuFragment = 0x03

        val map = HashMap<Int, String>()

        val destroyApp = Event(AppDestroy, 1 * 1000L, "AppDestroy")

        val destroyAppUpdate = Event(AppDestroyUpdate, 30 * 1000L, "AppDestroyUpdate")

        val destroyShortAppUpdate = Event(AppDestroyUpdate, 5 * 1000L, "AppDestroyUpdate")

        val destroyLongAppUpdate60 = Event(AppDestroyUpdate, 60 * 1000L, "AppDestroyUpdate")
        val destroyLongAppUpdate90 = Event(AppDestroyUpdate, 90 * 1000L, "AppDestroyUpdate")

        val navigateMenuFragment60 = Event(NavigateToMenuFragment, 60 * 1000L, "NavigateToMenuFragment")
    }

    init {
        map[AppDestroy] = "AppDestroy"
        map[AppDestroyUpdate] = "AppDestroyUpdate"
        map[NavigateToMenuFragment] = "NavigateToMenuFragment"
        map[RemoveNavigateToMenuFragment] = "RemoveNavigateToMenuFragment"
    }
}
