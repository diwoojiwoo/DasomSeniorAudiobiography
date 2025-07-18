package com.onethefull.dasomautobiography.base

/**
 * Created by sjw on 2021/11/11
 */
object OnethefullBase {
    const val ACTION_OPEN= "Open"
    const val CONTENTS_TYPE_DIARY = "CONTENTS_DIARY"
    const val PARAM_ACTION_NAME = "action"
    const val PARAM_QUIZ_COUNT= "count"
    const val PARAM_NEXT_SCENE_NAME = "NEXT_SCENE_NAME"
    const val PARAM_NEXT_SCENE_ACTION = "NEXT_SCENE_ACTION"

    // 상황 인식 변수
    const val PARAM_MOTION_DETECTED = "isMotionDetected" // 사람인식을 통해 실행됐는지
    const val PARAM_EFFECT_ON = "isEffectOn" // 효과음 on,off

}