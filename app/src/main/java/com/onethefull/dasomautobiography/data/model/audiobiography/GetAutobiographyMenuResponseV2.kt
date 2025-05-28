package com.onethefull.dasomautobiography.data.model.audiobiography

import android.os.Parcelable
import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/*--- 성함, 생년월일, 성별 존재 하지 않을 경우*/
//{
//    "status_code": 1001,
//    "status": "Not Exist",
//    "message": "어르신 성별/생년월일을 입력해주세요."
//}

/* 어르신이 등록되지 않은 경우*/
//{
//    "status_code": -3,
//    "status": "Non Registration Elderly",
//    "message": "어르신을 등록해주세요."
//}


/*--- 정상호출*/
//{
//    "status_code": 0,
//    "status": "ok",
//    "autobiographyList": [
//    {
//        "autobiographyId": 1,
//        "imgUrl": "https://www.googleapis.com/....",
//        "question": "윤희현어르신님의 성함의 뜻과 본적을 말씀해 주세요.",
//        "answerYn": "N",
//        "sort": 1,
//        "type": "init",
//        "viewQuestion": "성함의 뜻과 본적을 말씀해 주세요."
//    }....
//    ]
//}
data class GetAutobiographyMenuResponseV2(
    @SerializedName("status_code") @Expose var statusCode: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("message") @Nullable var message: String?,
    @SerializedName("autobiographyList") @Nullable var list: List<Entry>?,
)

@Parcelize
data class Entry(
    @SerializedName("autobiographyId") @Expose var id: Int,
    @SerializedName("audioUrl") @Expose var audioUrl: String,
    @SerializedName("transText") @Expose var transText: String,
    @SerializedName("imgUrl") @Expose var imgUrl: String,
    @SerializedName("question") @Expose var question: String,
    @SerializedName("answerYn") @Expose var answerYn: String,
    @SerializedName("sort") @Expose var sort: String,
    @SerializedName("type") @Expose var type: String,
    @SerializedName("typeName") @Nullable var typeName: String?,
    @SerializedName("viewQuestion") @Expose var viewQuestion: String
) : Parcelable
