package com.onethefull.dasomautobiography

import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.repository.SplashRepository
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import com.onethefull.wonderfulrobotmodule.robot.BaseRobotController
import com.onethefull.wonderfulrobotmodule.robot.IMotionCallback
import com.onethefull.wonderfulrobotmodule.robot.KebbiMotion
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 6. 11.
 */
class SplashViewModel(
    private val context: Activity,
    private val repository: SplashRepository
) : BaseViewModel(), GCTextToSpeech.Callback {
    // 발화 종류
    private val _speechType: MutableLiveData<SpeechType> = MutableLiveData<SpeechType>()
    val speechType: LiveData<SpeechType> = _speechType

    private val _isSpeechFinished = MutableLiveData(false)
    val isSpeechFinished: LiveData<Boolean> = _isSpeechFinished

    private val _isMotionDetected = MutableLiveData(false)
    val isMotionDetected: LiveData<Boolean> = _isMotionDetected

    private val _effectOn = MutableLiveData(false)
    val effectOn: LiveData<Boolean> = _effectOn

    init {
        connect()
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
        GCTextToSpeech.getInstance()?.setCallback(this)
        GCTextToSpeech.getInstance()?.start(context)
        BaseRobotController.initialize(App.instance)
    }

    fun disconnect() {
        DWLog.d("disconnect")
        GCTextToSpeech.getInstance()?.release()
        if (BaseRobotController.robotService != null) {
            try {
                BaseRobotController.robotService?.release()
            } catch (e: Exception) {
                DWLog.e("RobotService release() failed: ${e.message}")
            }
        } else {
            DWLog.w("RobotService disconnect() called but service is null")
        }
    }

    fun getContent() {
        uiScope.launch {
            if (_isMotionDetected.value == false) _speechType.value = SpeechType.INTRO_SPEECH
            val check204 = repository.check204() ?: false
            if (check204) {
                repository.getContent(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
                    Build.SERIAL,
                ).let { response ->
                    when (response.statusCode) {
                        0 -> {
                            response.autobiography?.let { item ->
                                (context as MainActivity).viewModel.selectItem(
                                    Entry(
                                        autobiographyId = item.id,
                                        audioUrl = "",
                                        transText = "",
                                        imgUrl = item.imgUrl,
                                        question = item.question,
                                        answerYn = "",
                                        sort = "",
                                        type = item.type,
                                        typeName = item.typeName,
                                        viewQuestion = item.viewQuestion,
                                    )
                                )
                            } ?: run {
                                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                                RxBus.publish(RxEvent.destroyApp)
                            }
                            if (_isMotionDetected.value == true) {
                                _speechType.value = SpeechType.CONTENT
                                return@let
                            }
                            GCTextToSpeech.getInstance()?.speech(response.introMent.toString())
                        }

                        else -> {
                            Toasty.error(context, context.getString(R.string.message_network_error)).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }
                    }
                }
            } else {
                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                RxBus.publish(RxEvent.destroyApp)
            }
        }
    }

    override fun onFinishService() {}

    override fun onWonderfulRequest(result: String) {}

    override fun onRequestHardwareControl(result: String) {}

    override fun onGenieSTTResult(result: String) {}

    override fun onGenieSTTResultNoAction(result: String) {}

    override fun onGenieCommandStart(result: String) {}

    override fun requestSendGenieLog(sttResult: String, genieResponse: String) {}

    suspend fun startMotionIntro() {
        _speechType.value = SpeechType.INTRO
        delay(500L)
        val speechText = arrayListOf(
            context.getString(R.string.message_intro_activity_recognition_1),
            context.getString(R.string.message_intro_activity_recognition_2),
            context.getString(R.string.message_intro_activity_recognition_3),
        ).random()


        delay(1000L)
        synchronized(this) {
            BaseRobotController.robotService?.robotMotor?.reset()
            BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.HANDS_UP, callback)
            GCTextToSpeech.getInstance()?.speech(speechText)
        }
    }

    private suspend fun startMotionFinish() {
        _speechType.value = SpeechType.END
        delay(500L)
        synchronized(this) {
            BaseRobotController.robotService?.robotMotor?.reset()
            BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.CALL_ACCEPT, callback)
            GCTextToSpeech.getInstance()?.speech(context.getString(R.string.message_finish_activity_recognition_1))
        }
    }

    fun setMotionDetected(value: Boolean) {
        _isMotionDetected.value = value
    }

    fun setEffectOn(value: Boolean) {
        _effectOn.value = value
    }

    override fun onSpeechStart() {
        DWLog.d("onSpeechStart")
        _isSpeechFinished.value = false
    }

    override fun onSpeechFinish() {
        DWLog.d("onSpeechFinish")
        _isSpeechFinished.value = true
        changeStatusSpeechFinished()
    }

    private fun changeStatusSpeechFinished() {
        DWLog.e("speechFinished ==> currentSpeechType ${_speechType.value}")
        when (_speechType.value) {
            SpeechType.INTRO -> {
                getContent()
            }

            SpeechType.INTRO_SPEECH -> {
                _speechType.value = SpeechType.CONTENT
            }

            SpeechType.END -> RxBus.publish(RxEvent.destroyApp)
            else -> {}
        }
    }

    var callback: IMotionCallback = object : IMotionCallback.Stub() {
        override fun finishMotion() {
            BaseRobotController.robotService?.robotMotor?.reset()
        }
    }
}