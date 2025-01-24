package com.onethefull.dasomautobiography.ui.diary

import android.app.Activity
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomautobiography.BuildConfig
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.data.model.NotExistData
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.repository.DiaryRepository
import com.onethefull.dasomautobiography.utils.WMediaPlayer
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.record.WavFileUitls
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Created by sjw on 2025. 1. 9.
 */
class DiaryViewModel(
    private val context: Activity,
    private val repository: DiaryRepository
) : BaseViewModel(), GCTextToSpeech.Callback {
    private var wavUtils = WavFileUitls()

    private val _date: MutableLiveData<String> = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private val _content: MutableLiveData<String> = MutableLiveData<String>()
    val content: LiveData<String> = _content

    private val _contentAudioUrl: MutableLiveData<String> = MutableLiveData<String>()
    val contentAudioUrl: LiveData<String> = _contentAudioUrl

    private val _data: MutableLiveData<NotExistData> = MutableLiveData<NotExistData>()
    val data: LiveData<NotExistData> = _data

    // 음성출력 상태
    private val _speechStatus: MutableLiveData<SpeechStatus> = MutableLiveData<SpeechStatus>()
    val speechStatus: LiveData<SpeechStatus> = _speechStatus

    // 일기 프로세스 상태
    private val _diaryStatus: MutableLiveData<DiaryStatus> = MutableLiveData<DiaryStatus>()
    val diaryStatus: LiveData<DiaryStatus> = _diaryStatus


    init {
        connect()
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
        GCTextToSpeech.getInstance()?.setCallback(this)
        GCTextToSpeech.getInstance()?.start(context)
        RxBus.publish(RxEvent.Event(RxEvent.AppDestroyUpdate, 90 * 1000L, "AppDestroyUpdate"))
    }

    fun disconnect() {
        GCTextToSpeech.getInstance()?.release()
        WMediaPlayer.instance.setListener(null)
    }

    fun intro() {
        _diaryStatus.value = DiaryStatus.INTRO
    }

    fun getDiarySentence() {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if (check204) {
                repository.getDiarySenetence(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
                    Build.SERIAL,
                    getDate(),
                    "D"
//                    "bokjiequip",
//                    "Dasom",
//                    "10390NNNB2234900026",
//                    "2025-01-05",
//                    "D"
                ).let { response ->
                    when (response.status_code) {
                        0 -> {
                            response.introTts?.let {
                                if (it.audioUrl.isNotEmpty() && BuildConfig.PRODUCT_TYPE != "KT")
                                    GCTextToSpeech.getInstance()?.urlMediaSpeech(it.audioUrl)
                                else
                                    GCTextToSpeech.getInstance()?.urlMediaSpeech(it.audioUrl)
                            }
                            response.diaryLog?.let {
                                _date.value = it.displayDate
                                _content.value = it.content
                            }
                            response.diaryTts?.let {
                                _contentAudioUrl.value = it.audioUrl
                            } ?: run { "" }
                        }

                        -97 -> {
                            showNotExitContent()
                        }

                        -1, -99 -> {
                            response.status.let {
                                RxBus.publish(RxEvent.destroyApp)
                            }
                        }

                        else -> {}
                    }
                }
            } else {
                RxBus.publish(RxEvent.destroyApp)
            }
        }

    }

    private fun getContent() {
        _diaryStatus.value = DiaryStatus.START
        synchronized(this) {
            _content.value = content.value
            if (contentAudioUrl.value != "" && BuildConfig.PRODUCT_TYPE != "KT")
                GCTextToSpeech.getInstance()?.urlMediaSpeech(_contentAudioUrl.value.toString())
            else
                GCTextToSpeech.getInstance()?.urlMediaSpeech(_contentAudioUrl.value.toString())
        }
    }

    private fun showNotExitContent() {
        Thread.sleep(2000L)
        _data.value = repository.getNotExistDiaryComment().random()
        _diaryStatus.value = data.value!!.status
        GCTextToSpeech.getInstance()?.speech(data.value!!.speechText)
    }

    private fun getDate(): String {
//        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val formattedDate = formatter.format(calendar.time)
        return formattedDate
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

    // 음성출력 시작
    private fun speechStarted() {
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
        DWLog.e("speechFinished ==> currentDiaryStatus ${_diaryStatus.value}")
        when (_diaryStatus.value) {
            DiaryStatus.INTRO -> {
                getContent()
            }

            DiaryStatus.START, DiaryStatus.NOT_EXIST_1, DiaryStatus.NOT_EXIST_2, DiaryStatus.NOT_EXIST_3 -> {
                RxBus.publish(RxEvent.destroyApp)
            }
        }

    }

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {
    }


    override fun onGenieSTTResult(result: String) {
    }
}