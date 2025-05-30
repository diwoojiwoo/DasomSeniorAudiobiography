package com.onethefull.dasomautobiography.ui.questiondetail

import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.audiobiography.DeleteLogResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.data.model.audiobiography.GetAutobiographyLogDtlResponse
import com.onethefull.dasomautobiography.data.model.audiobiography.Item
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.repository.QuestionDetailRepository
import com.onethefull.dasomautobiography.utils.ParamGeneratorUtils
import com.onethefull.dasomautobiography.utils.WMediaPlayer
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.record.VoiceRecorder
import com.onethefull.dasomautobiography.utils.record.WavFileUitls
import com.onethefull.dasomautobiography.utils.speech.GCSpeechToText
import com.onethefull.dasomautobiography.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import com.onethefull.dasomautobiography.utils.speech.GenieSpeechToTextImpl
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.IOException

/**
 * Created by sjw on 2025. 1. 9.
 */

class QuestionDetailViewModel(
    private val context: Activity,
    private val repository: QuestionDetailRepository
) : BaseViewModel(), GCSpeechToText.SpeechToTextCallback, GCTextToSpeech.Callback {
    private var mVoiceRecorder: VoiceRecorder? = null
    private var wavUtils = WavFileUitls()
    var isSuccessRecog = false

    // 음성출력 상태
    private val _speechStatus: MutableLiveData<SpeechStatus> = MutableLiveData<SpeechStatus>()
    val speechStatus: LiveData<SpeechStatus> = _speechStatus

    // 녹음 상태
    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    // 녹음 타이머 (1분)
    private val _timeLeft = MutableLiveData(60) // 60초 타이머
    val timeLeft: LiveData<Int> = _timeLeft

    private val _isRunning = MutableLiveData(false) // 타이머 실행 여부
    val isRunning: LiveData<Boolean> = _isRunning

    private val _currentItem = MutableLiveData<Entry>() // MainViewModel에서 공유받은 데이터
    val currentItem: LiveData<Entry> = _currentItem

    private var job: Job? = null // Coroutine Job

    private val _showDialog = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> get() = _showDialog

    private val _deleteEvent = MutableLiveData<Unit>()
    val deleteEvent: LiveData<Unit> get() = _deleteEvent

    // 녹음 파일 재생 하기
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    // 답변 가져오기
    private val _transText= MutableLiveData<String>()
    val transText: LiveData<String> get() = _transText

    private val _answerAudioUrl= MutableLiveData<String>()
    val answerAudioUrl: LiveData<String> get() = _answerAudioUrl

    private val _receivedLogId = MutableLiveData<String>()
    val receivedLogId : LiveData<String> get() = _receivedLogId


    init {
        connect()
    }

    fun fetchDataFromSharedViewModel(sharedViewModel: MainViewModel) {
        sharedViewModel.selectedItem.value?.let { item ->
            _currentItem.value = item
            DWLog.d("QuestionDetailViewModel: sharedViewModel에서 데이터 가져옴 -> $item")
        }
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
        GCTextToSpeech.getInstance()?.setCallback(this)
        GCTextToSpeech.getInstance()?.start(context)
        RxBus.publish(RxEvent.Event(RxEvent.AppDestroyUpdate, 90 * 1000L, "AppDestroyUpdate"))
    }

    fun disconnect() {
        DWLog.d("disconnect")
        GCTextToSpeech.getInstance()?.release()
        WMediaPlayer.instance.setListener(null)
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

    override fun onFinishService() {}

    override fun onWonderfulRequest(result: String) {}

    override fun onRequestHardwareControl(result: String) {}

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
        _speechStatus.value = SpeechStatus.WAITING
    }

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {}

    /***
     * GCSpeechToText
     * */
    override fun onSTTConnected() {}

    override fun onSTTDisconneted() {}

    override fun onVoice(data: ByteArray?, size: Int) {}

    override fun onVoiceStart() {}

    override fun onVoiceEnd() {}

    override fun onGenieSTTResult(result: String) {}

    override fun onGenieSTTResultNoAction(result: String) {}

    override fun onGenieCommandStart(result: String) {}

    override fun requestSendGenieLog(sttResult: String, genieResponse: String) {}

    override fun onVoiceResult(result: String?) {
//        isSuccessRecog = true
//        result?.let {
//            handleRecognition(result)
//        }
    }

    private fun handleRecognition(text: String) {}

    fun speech(text: String) {
        GCTextToSpeech.getInstance()?.speech(text)
    }

    /**
     * 음성입력 처리 콜백
     */
    private val voiceCallback = object : VoiceRecorder.Callback() {
        override fun onVoiceStart() {
            super.onVoiceStart()
//            DWLog.d("onVoiceStart")
            doStartVoice()
        }

        override fun onVoiceEnd() {
            super.onVoiceEnd()
//            DWLog.d("onVoiceEnd")
            doEndVoice()
        }

        override fun onVoice(data: ByteArray?, size: Int) {
            super.onVoice(data, size)
//            DWLog.e("onVoice")
            doOnVoice(data, size)
        }
    }

    /**
     * Recording 시작
     */
    private fun doStartVoice() {
        isSuccessRecog = false
        if (mVoiceRecorder != null) {
            mVoiceRecorder?.sampleRate?.let {
//                mService?.startRecognizing(it)
            }
            wavUtils?.startWavStream()
        }
    }

    /**
     * Recording 종료
     */
    private fun doEndVoice() {
        if (!isSuccessRecog) wavUtils?.finish()
    }

    /**
     * Recording 데이터 처리
     */
    private fun doOnVoice(data: ByteArray?, size: Int) {
        wavUtils?.writeAudioDataToFile(data)
    }

    /**
     * 음성 레코드 시작
     */
    private fun startVoiceRecorder(isBluetooth: Boolean = false) {
        DWLog.d("VoiceRecorder::startVoiceRecorder isBluetooth:$isBluetooth")
        if (mVoiceRecorder != null) {
            mVoiceRecorder?.stop()
        }
        mVoiceRecorder = VoiceRecorder(voiceCallback)
        try {
            DWLog.d("[GC] VoiceRecorder::startVoiceRecorder $mVoiceRecorder")
            mVoiceRecorder?.start()
        } catch (e: Exception) {
            DWLog.e("[GC] VoiceRecorder::Exception::" + e.message)
            e.printStackTrace()
        }
    }

    /**
     * 음성레코드 정지
     */
    private fun stopVoiceRecorder() {
        DWLog.d("VoiceRecorder::stopVoiceRecorder $mVoiceRecorder")
        if (mVoiceRecorder != null) {
            mVoiceRecorder?.stop()
            mVoiceRecorder = null
        }
    }

    fun startRecording() {
        RxBus.publish(RxEvent.destroyLongAppUpdate90)
        _isRecording.value = true  // 녹음 시작
        startVoiceRecorder()
    }

    fun stopRecording() {
        _isRecording.value = false // 녹음 종료
        stopVoiceRecorder()
    }

    fun startTimer() {
        if (_isRunning.value == true) return // 이미 실행 중이면 무시

        _isRunning.value = true
        _timeLeft.value = 59 // 59초 설정

        job = uiScope.launch {
            for (i in 58 downTo 0) { // 59→ 0초까지 감소
                delay(1000)
                _timeLeft.postValue(i)
            }
            _isRunning.postValue(false) // 59초 후 자동 종료
            stopRecording() // 레코딩 종료
        }
    }

    fun stopTimer() {
        job?.cancel() // 코루틴 중지
        _isRunning.value = false
    }

    fun triggerDialog() {
        _showDialog.value = true
    }

    fun playWavFile() {
        stopWavFile() // 기존 재생 중이면 정지

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource("${wavDirPath}${wavFileName}${wavExt}") // WAV 파일 경로 설정
                prepare() // 미디어 준비
                start() // 재생
                _isPlaying.value = true

                DWLog.d("WAV 파일 재생 시작: ${wavDirPath}${wavFileName}${wavExt}")

                setOnCompletionListener {
                    _isPlaying.value = false
                    releaseMediaPlayer()
                }
            } catch (e: IOException) {
                DWLog.e("WAV 파일 재생 실패: ${e.message}")
            }
        }
    }

    fun stopWavFile() {
        mediaPlayer?.apply {
            stop()
            releaseMediaPlayer()
            DWLog.d("WAV 파일 정지")
        }
    }

    fun pauseWavFile() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                _isPlaying.value = false
                DWLog.d("WAV 파일 일시 정지")
            }
        }
    }

    fun resumeWavFile() {
        mediaPlayer?.apply {
            start()
            _isPlaying.value = true
            DWLog.d("WAV 파일 재개")
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopWavFile() // ViewModel 종료 시 정리
    }

    private val _logDtlEvent = MutableLiveData<GetAutobiographyLogDtlResponse>()
    val logDtlEvent: LiveData<GetAutobiographyLogDtlResponse> get() = _logDtlEvent

    private val _networkErrorEvent = MutableLiveData<Boolean>()
    val networkErrorEvent: LiveData<Boolean> get() = _networkErrorEvent

    fun getLogDtl(autobiographyId : String) {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if (check204) {
                repository.getLogDtl(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
                    Build.SERIAL,
                    autobiographyId
                ).let { response ->
                    when (response.statusCode) {
                        1001 -> {
//                            Toasty.error(context, context.getString(R.string.message_not_exist_elderly_info)).show()
                            _logDtlEvent.postValue(GetAutobiographyLogDtlResponse(
                                statusCode = response.statusCode,
                                status = response.status ?: "",
                                message = context.getString(R.string.message_not_exist_elderly_info).toString() ?: null,
                                autobiographyMap = null
                            ))
                            RxBus.publish(RxEvent.destroyApp)
                        }

                        -3 -> {
//                            Toasty.error(context, context.getString(R.string.message_not_registration_elderly)).show()
                            _logDtlEvent.postValue(GetAutobiographyLogDtlResponse(
                                statusCode = response.statusCode,
                                status = response.status ?: "",
                                message = context.getString(R.string.message_not_registration_elderly).toString() ?: null,
                                autobiographyMap = null
                            ))
                            RxBus.publish(RxEvent.destroyApp)
                        }

                        0 -> {
                            response.autobiographyMap?.let { map ->
                                _logDtlEvent.postValue(GetAutobiographyLogDtlResponse(
                                    statusCode = 0,
                                    status = response.status,
                                    message = response.message ?:"",
                                    autobiographyMap = map
                                ))
                                _answerAudioUrl.value = map.answerAudioUrl ?: ""
                            } ?: run {
                                Toasty.error(context, "responseMap is null").show()
                                DWLog.e("responseMap is null")
                            }

                        }
                        else -> {
                            _logDtlEvent.postValue(GetAutobiographyLogDtlResponse(
                                statusCode = response.statusCode,
                                status = response.status ?: "",
                                message = "에러코드가 없습니다.",
                                autobiographyMap = null
                            ))
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

    /**
     * 자서전 로그 저장
     * */
    fun insertLog() {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if (check204) {
                repository.insertLog(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
//                    Build.SERIAL,
                    RequestBody.create(
                        MediaType.parse("text/plain"),
                        ParamGeneratorUtils.SERIAL_NUMBER
                    ),
//                    _currentItem.value?.id.toString(),
                    RequestBody.create(
                        MediaType.parse("text/plain"),
                        _currentItem.value?.autobiographyId.toString()
                    ),
//                    _currentItem.value?.type.toString(),
                    RequestBody.create(
                        MediaType.parse("text/plain"),
                        _currentItem.value?.type.toString()
                    ),
//                    "Y",
                    RequestBody.create(
                        MediaType.parse("text/plain"),
                        "Y"
                    ),
                    wavUtils.getMultipartWaveFile()
                ).let { response ->
                    when (response.statusCode) {
                        -99 -> {
                            Toasty.error(context, response.message.toString()).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }

                        -3 -> {
                            Toasty.error(context, context.getString(R.string.message_not_registration_elderly)).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }

                        0 -> {
                            triggerDialog()
                        }
                    }
                }
            } else {
                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                RxBus.publish(RxEvent.destroyApp)
            }
        }
    }

    /**
     * 자서전 로그 삭제
     * */
    fun deleteLog() {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if (check204) {
                val response: DeleteLogResponse = repository.deleteLog(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
                    Build.SERIAL,
                    _currentItem.value?.autobiographyId.toString()
                )

                when (response.status_code) {
                    -3 -> {
                        Toasty.error(context, context.getString(R.string.message_not_registration_elderly)).show()
                        RxBus.publish(RxEvent.destroyApp)
                    }

                    0 -> {
                        _deleteEvent.postValue(Unit)
                    }
                }
            } else {
                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                RxBus.publish(RxEvent.destroyApp)
            }
        }
    }

    fun startSpeech(text : String) {
        GCTextToSpeech.getInstance()?.speech(text)
    }

    fun startUrlSpeech(url : String) {
        if (url != "" && URLUtil.isValidUrl(url)) {
            GCTextToSpeech.getInstance()?.urlMediaSpeech(url)
        }
    }

    fun setReceivedLogId(logId: String) {
        if (logId.isNullOrBlank()) {
            DWLog.e("Invalid logId received: $logId")
            return
        }
        _receivedLogId.value = logId
        getLogDtl(logId)
    }

    companion object {
        private const val wavFileName = "recorded_bio_reply"
        private const val wavDirPath = "/sdcard/audio/temp/"
        private const val wavExt = ".wav"
    }
}