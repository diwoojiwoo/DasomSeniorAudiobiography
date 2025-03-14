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
//    "status": "ok",
//    "autobiographyMap": {
//    "imgUrl": "https://www.googleapis.com/download/storage/v1/b/new_silvercare_dev/o/autobiographyQuestion%2F2025-01-08_14:27:37_ByRGca.png?generation=1736314059095121&alt=media",
//    "autobiographyId": 1,
//    "audioUrl": "https://www.googleapis.com/download/storage/v1/b/new_silvercare_dev/o/prodbeta%2Fautobiography%2F2025-02-03_08:31:02_pCVDxh.wav?generation=1738539063464457&alt=media",
//    "transText": "휴대폰 전체로 물어봐야지 그 돼",
//    "autobiographyLogId": 49,
//    "question": "윤희현어르신님의 성함의 뜻과 본적을 말씀해 주세요.",
//    "typeName": "첫 질문",
//    "answerYn": "Y",
//    "type": "init",
//    "viewQuestion": "성함의 뜻과 본적을 말씀해 주세요."
//}
//}

data class GetAutobiographyLogDtlResponse(
    @SerializedName("status_code") @Expose var statusCode: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("message") @Nullable var message: String?,
    @SerializedName("autobiographyMap") @Nullable var autobiographyMap: AutobiographyMap?,
)

data class AutobiographyMap(
    @SerializedName("imgUrl") @Expose var imgUrl: String,
    @SerializedName("autobiographyId") @Expose var id: Int,
    @SerializedName("transText") @Expose var transText: String,
    @SerializedName("autobiographyLogId") @Expose var autobiographyLogId: Int,
    @SerializedName("question") @Expose var question: String,
    @SerializedName("typeName") @Expose var typeName: String,
    @SerializedName("answerYn") @Expose var answerYn: String,
    @SerializedName("type") @Expose var type: String,
    @SerializedName("answerAudioUrl") @Expose var answerAudioUrl : String,
    @SerializedName("viewQuestion") @Expose var viewQuestion: String,
)
