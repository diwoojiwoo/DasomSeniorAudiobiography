package com.onethefull.dasomautobiography.utils

import com.onethefull.dasomautobiography.data.model.audiobiography.Item
import com.onethefull.dasomautobiography.ui.menu.MenuItem

/**
 * Created by sjw on 2025. 3. 14.
 */
class MenuItemToItemMapper {
    fun map(menuItem: MenuItem): Item {
        return Item(
            id =0, // 기본값
            logId = 0, // 기본값
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