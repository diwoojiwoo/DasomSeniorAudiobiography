package com.onethefull.dasomcontents.data.model.memory

import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class InsertMemoryQuizLogResponse(
    @SerializedName("status_code") @Expose var status_code: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("message") @Nullable var message: String? = null,
)
