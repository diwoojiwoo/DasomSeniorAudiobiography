package com.onethefull.dasomautobiography.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.onethefull.dasomautobiography.ui.diary.DiaryStatus


/**
 * Created by sjw on 2025/01/10
 */

data class NotExistData(
    @SerializedName("status") @Expose var status: DiaryStatus,
    var image :Int,
    var title: String,
    var detail : String,
    var speechText : String
)