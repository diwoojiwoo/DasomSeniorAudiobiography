package com.onethefull.dasomcontents.base

import android.net.Uri
import com.onethefull.dasomcontents.App

/**
 * Created by sjw on 2021/11/11
 */
object OnethefullBase {
    const val PARAM_PRAC_TYPE = "PRAC_TYPE_PARAM"
    const val PARAM_LIMIT = "limit"
    const val PARAM_CATEGORY = "category"
    const val PARAM_CONTENT = "content"
    const val PARAM_URL = "url"
    const val PARAM_TITLE = "title"
    const val PARAM_NEXT_CONTENT = "next_content"


    const val QUIZ_TYPE_SHOW = "Quiz_show"
    const val MEAL_TYPE_SHOW = "Meal_show"
    const val MEAL_TYPE_FINISH = "Meal_finish"
    const val PRACTICE_EMERGENCY = "practice_emergency"
    const val KEBBI_TUTORIAL_SHOW = "Kebbi_Tutorial_Show"

    const val DEMO_AD_WALMART = "demo_ad_walmart"
    const val DEMO_AD_UBER = "demo_ad_uber"
    const val DEMO_AD_RANDOM = "demo_ad_random"


    val uri_walmart = Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/walmart")
    val uri_uber = Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/uber")

    const val start = "start"
    const val start_walmart = "w_start"
    const val start_uber = "u_start"

    const val stop_video_walmart = "w_video_stop"
    const val stop_video_uber = "u_video_stop"

    const val recognition_walmart = "w_recognition"
    const val recognition_uber = "u_recognition"

    const val finish_walmart = "w_finish"
    const val finish_uber = "u_finish"

    const val SLEEP_TIME_NAME = "sleepTime"
    const val WAKEUP_TIME_NAME = "wakeupTime"
    const val BREAKFAST_NAME = "breakfast"
    const val LUNCH_NAME = "lunch"
    const val DINNER_NAME = "dinner"
    const val BREAKFAST_TIME_NAME = "breakfastTime"
    const val LUNCH_TIME_NAME = "lunchTime"
    const val DINNER_TIME_NAME = "dinnerTime"


    const val BREAKFAST_NAME_KOR = "아침식사"
    const val LUNCH_NAME_KOR = "점심식사"
    const val DINNER_NAME_KOR = "저녁식사"
    const val TIME_NAME_KOR_ = "몇시"

    const val GUIDE_TYPE_PARAM = "GUIDE_TYPE_PARAM"
    const val GUIDE_WAKEUP = "guideWakeup"
    const val GUIDE_VISION = "guideVision"
    const val GUIDE_MEDICATION = "guideMedi"
    const val GUIDE_COMMUNITY = "guideComm"
    const val GUIDE_MONITORING = "guideMoni"
    const val GUIDE_MESSAGE = "guideMessage"
    const val GUIDE_EMERGENCY = "guide_emergency"

    // FOCUS
    const val CAMERA_FOCUS_NAME = "camera"

    const val MODULE_NAME_YOUTUBE = "Youtube"
    const val ACTION_YOUTUBE_PLAY_DEMO = "VideoUrl_play_Demo"

    const val CONTENT_DASOMTALK = "DasomTalk"
    const val CONTENT_VIDEO = "Video"
    const val CONTENT_RADIO = "Radio"
    const val CONTENT_SOS = "Sos"
    const val CONTENT_MEDICATION = "Medication"
    const val CONTENT_MV = "Mv"

    const val WALMART = "walmart"
    const val UBER = "uber"

}