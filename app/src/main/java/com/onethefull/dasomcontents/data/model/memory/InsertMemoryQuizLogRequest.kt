package com.onethefull.dasomcontents.data.model.memory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


//{
//    "serialNum": "serialNum",
//    "quizId": "제공받은 quizId",
//    "question": "문제 질문",
//    "answer": "문제 정답",
//    "content": "발화 내용",
//    "passYn": "Y-정답,N-오답"
//}
data class InsertMemoryQuizLogRequest(
    @SerializedName("serialNum") @Expose val serialNum: String,
    @SerializedName("quizId") @Expose val quizId: String,
    @SerializedName("question") @Expose val question: String,
    @SerializedName("answer") @Expose val answer: String,
    @SerializedName("content") @Expose val content: String,
    @SerializedName("passYn") @Expose val passYn: String,
)