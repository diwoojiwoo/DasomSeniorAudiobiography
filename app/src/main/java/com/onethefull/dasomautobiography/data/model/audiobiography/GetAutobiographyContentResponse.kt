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
//--- 자서전 질문을 다 한 경우
//{
//    "status_code": -97,
//    "status": "Not Exist",
//    "message": "자서전 질문이 없습니다."
//}
//--- 정상호출
//{
//    "status_code": 0,
//    "status": "ok",
//    "autobiography": {
//    "autobiographyId": 38,
//    "question": "2020년, 코로나가 발생 후 생활에 많은 변화가 일어났습니다. 코로나 이후 생활에서 가장 변화된 건 어떤 것들이 있었는지 알려주세요.",
//    "type": "social",
//    "viewQuestion": "2020년 코로나 이후 생활에서 가장 변화된 건 어떤 것들이 있었는지 알려주세요.",
//    "imgUrl": "https://www.googleapis.com/...."
//}
//}
data class GetAutobiographyContentResponse(
    @SerializedName("status_code") @Expose var statusCode: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("message") @Nullable var message: String?,
    @SerializedName("autobiography") @Nullable var autobiography: Autobiography?,
)

data class Autobiography(
    @SerializedName("autobiographyId") @Expose var id: Int,
    @SerializedName("question") @Expose var question: String,
    @SerializedName("type") @Expose var type: String,
    @SerializedName("viewQuestion") @Expose var viewQuestion: String,
    @SerializedName("imgUrl") @Expose var imgUrl: String
)
