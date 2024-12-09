package com.onethefull.dasomcontents.ui.memoryquiz


import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson

import com.onethefull.dasomcontents.base.BaseViewModel
import com.onethefull.dasomcontents.data.model.memory.MemoryQuiz
import com.onethefull.dasomcontents.data.model.memory.SelectMemoryQuizResponse
import com.onethefull.dasomcontents.repository.MemoryContentRepository
import com.onethefull.dasomcontents.utils.Resource
import com.onethefull.dasomcontents.utils.WMediaPlayer
import com.onethefull.dasomcontents.utils.bus.RxBus
import com.onethefull.dasomcontents.utils.bus.RxEvent
import com.onethefull.dasomcontents.utils.logger.DWLog
import com.onethefull.dasomcontents.utils.record.WavFileUitls
import com.onethefull.dasomcontents.utils.speech.GCSpeechToText
import com.onethefull.dasomcontents.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomcontents.utils.speech.GCTextToSpeech
import com.onethefull.dasomcontents.utils.speech.SpeechStatus
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2024. 12. 4.
 */
class MemoryContentViewModel(
    private val context: Activity,
    private val repository: MemoryContentRepository
) : BaseViewModel(), GCSpeechToText.SpeechToTextCallback, GCTextToSpeech.Callback {
    private var mGCSpeechToText: GCSpeechToText = GCSpeechToTextImpl(context)
    private var wavUtils = WavFileUitls()

    private val _question: MutableLiveData<String> = MutableLiveData<String>()
    val question: LiveData<String> = _question

    private val _answer: MutableLiveData<String> = MutableLiveData<String>()
    val answer: LiveData<String> = _answer

    private val _imgUrl: MutableLiveData<String> = MutableLiveData<String>()
    val imgUrl: LiveData<String> = _imgUrl

    private val _hint: MutableLiveData<String> = MutableLiveData<String>()
    val hint: LiveData<String> = _hint

    private val _hintImgUrl: MutableLiveData<String> = MutableLiveData<String>()
    val hintImgUrl: LiveData<String> = _hintImgUrl

    private val _toAnswer: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val toAnswer: LiveData<Boolean> = _toAnswer

    private val _inputText: MutableLiveData<String> = MutableLiveData<String>()
    val inputText: LiveData<String> = _inputText

    //
    private val _quizComments = MutableLiveData<Resource<String>>()
    fun quizComments(): LiveData<Resource<String>> {
        return _quizComments
    }

    var isSuccessRecog = false

    // 음성출력 상태
    private val _speechStatus: MutableLiveData<SpeechStatus> = MutableLiveData<SpeechStatus>()
    val speechStatus: LiveData<SpeechStatus> = _speechStatus

    fun checkQuizStatus(status: MemoryQuizStatus) {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if (check204) {
                val response: SelectMemoryQuizResponse = repository.selectMemoryQuiz()
                when (response.status_code) {
                    0 -> {
                        response.memoryQuiz?.let {
                            synchronized(this) {
                                GCTextToSpeech.getInstance()?.speech(it.question)
                                _question.value = it.question
                                _answer.value = "정답 : ${it.answer}"
                                _imgUrl.value = it.imgUrl
                                it.hint?.let { _hint.value = it } ?: run { "" }
                                it.hintImgUrl?.let { _hintImgUrl.value = it } ?: run { "" }
//                                _quizComments.postValue(Resource.success(Gson().toJson(response.memoryQuiz)))
                            }
                        }
                    }

                    -1 -> {
                        response.status.let {
                            _quizComments.postValue(Resource.error(it, null))
                        }
                    }

                    -99 -> {
                        response.status.let {
                            _quizComments.postValue(Resource.error(it, null))
                        }
                    }

                    -97 -> {
                        response.status.let {
                            _quizComments.postValue(Resource.error(it, null))
                        }
                    }

                }
            } else {
                _quizComments.postValue(Resource.error("Network error", null))
            }
        }
    }

    init {
        connect()
        mGCSpeechToText.setCallback(this)
        mGCSpeechToText.setWavUtils(wavUtils)
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
        mGCSpeechToText.start()
        GCTextToSpeech.getInstance()?.setCallback(this)
        GCTextToSpeech.getInstance()?.start(context)
        RxBus.publish(RxEvent.Event(RxEvent.AppDestroyUpdate, 90 * 1000L, "AppDestroyUpdate"))
    }

    fun disconnect() {
        mGCSpeechToText.release()
        GCTextToSpeech.getInstance()?.release()
        WMediaPlayer.instance.setListener(null)
    }

    /***
     * GCSpeechToText
     * */
    override fun onSTTConnected() {}

    override fun onSTTDisconneted() {}

    override fun onVoice(data: ByteArray?, size: Int) {}

    override fun onVoiceStart() {}

    override fun onVoiceEnd() {}

    override fun onGenieSTTResult(result: String) {}

    override fun onVoiceResult(result: String?) {
        isSuccessRecog = true
        result?.let {
            handleRecognition(result)
        }
    }

    private fun handleRecognition(text: String) {
        DWLog.d("handleRecognition input ==> $text")
        if (text == GCSpeechToTextImpl.ERROR_OUT_OF_RANGE) {
            DWLog.e("GCSpeechToTextImpl.ERROR_OUT_OF_RANGE")
            RxBus.publish(RxEvent.destroyApp)
            return
        }

        uiScope.launch {
            _inputText.value = text

            if(text == answer.value) {
                _toAnswer.value = true
                RxBus.publish(RxEvent.destroyApp)
            } else {
                _toAnswer.value = false
                RxBus.publish(RxEvent.destroyAppUpdate)
            }
        }
    }

    // 음성출력 시작
    private fun speechStarted() {
        mGCSpeechToText.pause()
        _speechStatus.value = SpeechStatus.SPEECH
    }

    // 음성출력 종료
    private fun speechFinished() {
        changeStatusSpeechFinished()
        checkCurrentStatus()
    }

    /**
     * TTS 출력이 끝난 상태 변경
     */
    private fun changeStatusSpeechFinished() {
        mGCSpeechToText.resume()
        _speechStatus.value = SpeechStatus.WAITING
    }

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {

    }

    /***
     * GCTextToSpeech
     * */
    // TTS 출력 시작
    override fun onSpeechStart() {
        DWLog.d("onSpeechStart")
        speechStarted()
    }

    // TTS 출력 종료
    override fun onSpeechFinish() {
        DWLog.d("onSpeechFinish")
        speechFinished()
    }

}
