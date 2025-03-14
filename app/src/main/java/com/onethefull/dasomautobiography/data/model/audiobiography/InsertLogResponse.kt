package com.onethefull.dasomautobiography.data.model.audiobiography

import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

//--- 어르신이 등록되지 않은 경우
//{
//    "status_code": -3,
//    "status": "Non Registration Elderly",
//    "message": "어르신을 등록해주세요."
//}
//--- 정상호출
//{
//    "status_code": 0,
//    "status": "ok"
//}
data class InsertLogResponse(
    @SerializedName("status_code") @Expose var statusCode: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("message") @Nullable var message : String?,
)
