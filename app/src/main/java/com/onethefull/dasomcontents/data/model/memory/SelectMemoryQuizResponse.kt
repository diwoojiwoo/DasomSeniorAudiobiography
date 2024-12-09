package com.onethefull.dasomcontents.data.model.memory

import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

// status_code = 0 - ok
// status_code = -1 - error
// status_code = -99  message = Not Exist Required Param
// "status_code": -97, "status": "Not Exist"


//{
//    "status_code": 0,
//    "status": "ok",
//    "memoryQuiz": {
//      "imgUrl": "https://www.googleapis.com/download/storage/v1/b/new_silvercare/o/prodbeta%2Fdementia%2Fmemory%2F2024-12-06_13:36:50_ABDsQs.png?generation=1733459810614782&alt=media",
//      "answerCn": "test",
//      "questionCn": "test",
//      "question": "test",
//      "answer": "test",
//      "quizId": 79,
//      "memoryId": 53,
//      "questionEn": "test",
//      "answerEn": "test",
//      "hintImgUrl": "https://www.googleapis.com/download/storage/v1/b/new_silvercare/o/prodbeta%2Fdementia%2Fmemory%2Fhint%2F2024-12-06_13:36:50_aKTEob.png?generation=1733459810956413&alt=media"
//}

data class SelectMemoryQuizResponse(
    @SerializedName("status_code") @Expose var status_code: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("message") @Nullable var message: String?,
    @SerializedName("memoryQuiz") @Nullable var memoryQuiz: MemoryQuiz? = null
)

data class MemoryQuiz(
    @SerializedName("question") @Expose var question: String,
    @SerializedName("questionEn") @Expose var questionEn: String,
    @SerializedName("questionCn") @Expose var questionCn: String,
    @SerializedName("answer") @Expose var answer: String,
    @SerializedName("answerEn") @Expose var answerEn: String,
    @SerializedName("answerCn") @Expose var answerCn: String,
    @SerializedName("quizId") @Expose var quizId: Int,
    @SerializedName("memoryId") @Expose var memoryId: Int,
    @SerializedName("imgUrl") @Expose var imgUrl: String,

    // 힌트는 텍스트 또는 이미지 둘중 하나만 가능
    @SerializedName("hintImgUrl") @Nullable var hintImgUrl: String?,
    @SerializedName("hint") @Nullable var hint: String?,
)