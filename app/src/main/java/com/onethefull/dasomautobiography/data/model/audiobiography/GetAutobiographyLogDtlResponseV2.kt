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

data class GetAutobiographyLogDtlResponseV2(
    @SerializedName("status_code") @Expose var statusCode: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("deleteCnt") @Nullable var deleteCnt: Int?, // 0 이상일 경우 삭제한 답변이 있는 경우(보호자앱 사용)
    @SerializedName("autobiographyMap") @Nullable var autobiographyMap: TotalMap?,
    @SerializedName("message") @Nullable var message: String?,
)

data class TotalMap(
    @SerializedName("imgUrl") @Expose var imgUrl: String,
    @SerializedName("autobiographyId") @Expose var id: Int,
    @SerializedName("guardianComment") @Expose var guardianComment: String, // 5월 23일 추가
    @SerializedName("autobiographyList") @Nullable var mapList: List<Map>?,
    @SerializedName("question") @Expose var question: String,
    @SerializedName("typeName") @Expose var typeName: String,
    @SerializedName("type") @Expose var type: String,
    @SerializedName("viewQuestion") @Expose var viewQuestion: String,
)

data class Map (
    @SerializedName("autobiographyId") @Expose var id: Int,
    @SerializedName("autobiographyLogId") @Expose var autobiographyLogId: Int,
    @SerializedName("answerYn") @Expose var answerYn: String,
    @SerializedName("deleteYn") @Expose var deleteYn: String,
    @SerializedName("answerAudioUrl") @Expose var answerAudioUrl : String,
    @SerializedName("createAt") @Expose var createAt : String,
)