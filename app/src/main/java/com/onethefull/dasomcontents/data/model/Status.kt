package com.onethefull.dasomcontents.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by sjw on 2021/11/23
 */
data class Status(
    @SerializedName("status_code") @Expose var status_code: Int?,
    @SerializedName("status") @Expose var status: String?
)
