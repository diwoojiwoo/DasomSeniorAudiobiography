package com.onethefull.dasomiconv.data.filter

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by sjw on 2024/12/6.
 */

//{
//    "projectName": "dasom",
//    "appId":"dasom2",
//    "domain": "problems",
//    "clientId": "jin3137",
//    "customerCode": "prodbeta",
//    "query": "삼",
//    "languageCode": "ko",
//    "problem": "문제: 1 + 1 = ? 무엇인가?",
//    "correctAnswer": "주어진 답변: 2"
//}
data class NlpProblemsRequestData(
    @SerializedName("projectName") @Expose val projectName: String,
    @SerializedName("appId") @Expose val appId: String,
    @SerializedName("domain") @Expose val domain: String,
    @SerializedName("clientId") @Expose val clientId: String,
    @SerializedName("customerCode") @Expose val customerCode: String,
    @SerializedName("query") @Expose val query: String,
    @SerializedName("languageCode") @Expose val languageCode: String,
    @SerializedName("problem") @Expose val problem: String,
    @SerializedName("correctAnswer") @Expose val correctAnswer: String,
    )
