package com.onethefull.dasomautobiography.data.model.audiobiography

import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

//--- 성함, 생년월일, 성별 존재 하지 않을 경우
//{
//    "status_code": 1001,
//    "status": "Not Exist",
//    "message": "어르신 성별/생년월일을 입력해주세요."
//}
//--- 어르신이 등록되지 않은 경우
//{
//    "status_code": -3,
//    "status": "Non Registration Elderly",
//    "message": "어르신을 등록해주세요."
//}
//
//--- 정상호출
//{
//    "status_code": 0,
//    "status": "ok"
//}
data class DeleteLogResponse(
    @SerializedName("status_code") @Expose var status_code: Int,
    @SerializedName("status") @Expose var status: String,
//    @SerializedName("message") @Nullable var message: String?,
)
