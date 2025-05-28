package com.onethefull.dasomautobiography.ui.menu

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by sjw on 2025. 2. 10.
 */
@Parcelize
data class MenuItem2(
    @SerializedName("imgUrl") @Expose var imgUrl: String,
    @SerializedName("typeName") @Expose var typeName: String,
    @SerializedName("questionCnt") @Expose var questionCnt: Int,
    @SerializedName("answerCnt") @Expose var answerCnt: Int,
    @SerializedName("type") @Expose var type: String,
    @SerializedName("latestCreatedAt") @Expose var latestCreatedAt: String,
    @SerializedName("per") @Expose var per: Double,
) : Parcelable
