package com.onethefull.dasomautobiography.manager

import com.onethefull.dasomautobiography.data.model.audiobiography.Ment

/**
 * Created by sjw on 2025. 11. 20.
 */
object MentManager {
    var smartfriendMent: Ment? = null
    var commandMent: Ment? = null
    var currentActionName: String? = null

    fun clear() {
        smartfriendMent = null
        commandMent = null
        currentActionName = null

    }
}