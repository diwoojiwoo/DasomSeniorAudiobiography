package com.onethefull.dasomautobiography.utils

import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.data.model.audiobiography.Item
import com.onethefull.dasomautobiography.ui.menu.MenuItem
import com.onethefull.dasomautobiography.ui.menu.MenuItem2

/**
 * Created by sjw on 2025. 3. 14.
 */
class MenuItemToEntryMapper {
    fun map(menuItem: MenuItem2): Entry {
        return Entry(
            autobiographyId =0, // 기본값
            audioUrl = "", // 기본값
            transText = "", // 기본값
            imgUrl = menuItem.imgUrl,
            question = "", // 기본값
            answerYn = if (menuItem.answerCnt > 0) "Y" else "N",
            sort = "0", // 기본값
            type = menuItem.type,
            typeName = menuItem.typeName,
            viewQuestion = "" // 기본값
        )
    }
}