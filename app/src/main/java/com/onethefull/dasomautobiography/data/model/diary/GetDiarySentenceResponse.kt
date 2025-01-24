package com.onethefull.dasomautobiography.data.model.diary

import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

// status_code = 0 - ok
// status_code = -1 - error
// status_code = -99  message = Not Exist Required Param
// "status_code": -97, "status": "Not Exist"
//{
//    "status_code": 1001,
//    "status": "Not Exist",
//    "message": "어르신이 등록되어있지 않습니다."
//}


//{
//    "status_code": 0,
//    "status": "ok",
//    "diaryLog": {
//    "date": "2025-01-05",
//    "createdAt": "2025-01-06 01:49:27",
//    "serialNum": "10390NNNB2234900026",
//    "dateEn": "January 05 2025",
//    "displayDate": "2025년 01월 05일 일요일",
//    "ttsDate": "2025년 1월 5일",
//    "displayDateEn": "Sun, January 05 2025",
//    "type": "D",
//    "content": "2025년 1월 5일 월요일 박헌식님의 하루를 전해드릴게요. 오늘 박헌식님은 아침에는 특별한 활동을 하지 않으시고 차분하게 하루를 시작하셨네요. 따뜻한 차 한잔과 함께 여유로운 시간을 보내셨을 것 같아요. 오후가 되면서 다솜톡에서 유튜브를 통해 4개의 영상을 시청하셨다는 소식이 들어왔어요. 요즘 임영웅님의 영상에 푹 빠지셨나 봐요 그 외에도 긍정적 단어를 많이 사용하고 계시네요. '좋음'이라는 단어가 자주 등장했답니다. 긍정적인 마음가짐이 참 멋지세요 하지만 오늘은 복약 알람이 울리긴 했지만, 약을 챙기지 않으신 것 같아요. 건강을 위해 약을 드시는 것을 잊지 않도록 조금 더 신경 써주셨으면 해요. 오늘 하루 포근하고 잔잔하게 잘 보내신 것 같아요. 내일도 즐거운 하루 보내시길 바랍니다"
//},
//    "introTts": {
//    "audioUrl": "https://storage.googleapis.com/new_silvercare/tts/dailySentence/ko-KR/2025-01-07/2025-01-07_15_47_33_fkBLFM.mp3",
//    "voiceCode": "ko-KR-Neural2-A",
//    "serviceCode": "google",
//    "text": "박헌식님의 2025년 1월 5일 다솜 일기입니다.",
//    "languageCode": "ko-KR",
//    "key": "diaryIntro"
//},
//    "diaryTts": {
//    "audioUrl": "https://storage.googleapis.com/new_silvercare/tts/dailySentence/ko-KR/2025-01-07/2025-01-07_15_47_36_qoTpxB.mp3",
//    "voiceCode": "ko-KR-Neural2-A",
//    "serviceCode": "google",
//    "text": "2025년 1월 5일 월요일 박헌식님의 하루를 전해드릴게요. 오늘 박헌식님은 아침에는 특별한 활동을 하지 않으시고 차분하게 하루를 시작하셨네요. 따뜻한 차 한잔과 함께 여유로운 시간을 보내셨을 것 같아요. 오후가 되면서 다솜톡에서 유튜브를 통해 4개의 영상을 시청하셨다는 소식이 들어왔어요. 요즘 임영웅님의 영상에 푹 빠지셨나 봐요 그 외에도 긍정적 단어를 많이 사용하고 계시네요. '좋음'이라는 단어가 자주 등장했답니다. 긍정적인 마음가짐이 참 멋지세요 하지만 오늘은 복약 알람이 울리긴 했지만, 약을 챙기지 않으신 것 같아요. 건강을 위해 약을 드시는 것을 잊지 않도록 조금 더 신경 써주셨으면 해요. 오늘 하루 포근하고 잔잔하게 잘 보내신 것 같아요. 내일도 즐거운 하루 보내시길 바랍니다",
//    "languageCode": "ko-KR",
//    "key": "diaryContent"
//}
//}

data class GetDiarySentenceResponse(
    @SerializedName("status_code") @Expose var status_code: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("diaryLog") @Nullable var diaryLog: DiaryLog?,
    @SerializedName("introTts") @Nullable var introTts : IntroTts?,
    @SerializedName("diaryTts") @Nullable var diaryTts : DiaryTts?

)
data class DiaryLog(
    @SerializedName("date") @Expose var date: String,
    @SerializedName("createdAt") @Expose var createdAt: String,
    @SerializedName("serialNum") @Expose var serialNum: String,
    @SerializedName("dateEn") @Expose var dateEn: String,
    @SerializedName("displayDate") @Expose var displayDate: String,
    @SerializedName("ttsDate") @Expose var ttsDate: String,
    @SerializedName("displayDateEn") @Expose var displayDateEn: String,
    @SerializedName("type") @Expose var type: String,
    @SerializedName("content") @Expose var content: String,
)

data class IntroTts (
    @SerializedName("audioUrl") @Expose var audioUrl: String,
    @SerializedName("voiceCode") @Expose var voiceCode: String,
    @SerializedName("serviceCode") @Expose var serviceCode: String,
    @SerializedName("text") @Expose var text: String,
    @SerializedName("languageCode") @Expose var languageCode: String,
    @SerializedName("key") @Expose var key: String,
)

data class DiaryTts(
    @SerializedName("audioUrl") @Expose var audioUrl: String,
    @SerializedName("voiceCode") @Expose var voiceCode: String,
    @SerializedName("serviceCode") @Expose var serviceCode: String,
    @SerializedName("text") @Expose var text: String,
    @SerializedName("languageCode") @Expose var languageCode: String,
    @SerializedName("key") @Expose var key: String,
)