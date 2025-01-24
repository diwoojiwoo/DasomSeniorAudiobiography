package com.onethefull.dasomautobiography.data.model.chatbot

import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by sjw on 2024/12/6.
 */

//{
//    "appId": "dasom2",
//    "assistant": "***",
//    "customerCode": "prodbeta",
//    "data": {
//    "actionName": "sovle_problems",
//    "categoryName": "",
//    "serviceName": "sovle_problems"
//},
//    "domain": "smartfriends",
//    "gpt_datas": {
//    "answer": "오답",
//    "answer_code": "N",
//    "description": "사용자가 입력한 답변 '삼'은 주어진 답 '2'와 다릅니다. 숫자 형태의 답을 기대했지만 문자가 제공되었습니다. 유사도 점수 계산이 적용되지 않습니다."
//},
//    "hint": "오답",
//    "robot_action": {
//    "expressionCodes": [
//    "kid_smile"
//    ],
//    "motionCodes": [
//    "666_TA_LookDnU"
//    ]
//}

data class NlpProblemsResponseData(
    @SerializedName("appId") @Expose var appId: String,
    @SerializedName("assistant") @Expose var assistant: String,
    @SerializedName("customerCode") @Expose var customerCode: String,
    @SerializedName("data") @Expose var data: Data,
    @SerializedName("domain") @Expose var domain: String,
    @SerializedName("gptDatas") @Expose var gptDatas: GPTDatas,
    @SerializedName("hint") @Nullable var hint: String?,
    @SerializedName("robotAction") @Expose var robotAction: RobotAction,
)

data class Data(
    @SerializedName("actionName") @Expose var actionName : String,
    @SerializedName("categoryName") @Expose var categoryName : String,
    @SerializedName("serviceName") @Expose var serviceName : String,
)

data class GPTDatas(
    @SerializedName("answer") @Expose var answer : String,
    @SerializedName("answer_code") @Expose var answer_code : String,
    @SerializedName("description") @Expose var description : String,
)

data class RobotAction(
    @SerializedName("expressionCodes") @Expose var expressionCodes: List<String>,
    @SerializedName("motionCodes") @Expose var motionCodes: List<String>
)