package com.onethefull.dasomautobiography.ui.speech

import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import android.os.RemoteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.BuildConfig
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.SpeechType
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.Status
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.data.model.audiobiography.Item
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.repository.SpeechRepository
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.ParamGeneratorUtils
import com.onethefull.dasomautobiography.utils.Product
import com.onethefull.dasomautobiography.utils.WMediaPlayer
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.bus.RxEvent.Companion.AppDestroyUpdate
import com.onethefull.dasomautobiography.utils.bus.RxEvent.Companion.NavigateToMenuFragment
import com.onethefull.dasomautobiography.utils.bus.RxEvent.Companion.RemoveNavigateToMenuFragment
import com.onethefull.dasomautobiography.utils.bus.RxEvent.Event
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.record.VoiceRecorder
import com.onethefull.dasomautobiography.utils.record.WavFileUitls
import com.onethefull.dasomautobiography.utils.record.WavFileUitls.Companion
import com.onethefull.dasomautobiography.utils.speech.GCSpeechToText
import com.onethefull.dasomautobiography.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import com.onethefull.dasomautobiography.utils.speech.GenieSpeechToTextImpl
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus
import com.onethefull.wonderfulrobotmodule.robot.BaseRobotController
import com.onethefull.wonderfulrobotmodule.robot.IMotionCallback
import com.onethefull.wonderfulrobotmodule.robot.KebbiMotion
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.IOException

/**
 * Created by sjw on 2025. 1. 9.
 */

class SpeechViewModel(
    private val context: Activity,
    private val repository: SpeechRepository
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
    var recordStatus: RecordStatus = RecordStatus.INIT

    // 재생 상태
    private var mediaPlayer: MediaPlayer? = null
    var playStatus: PlayStatus = PlayStatus.INIT

    // MainViewModel에서 공유받은 데이터
    private val _currentItem = MutableLiveData<Entry>()
    val currentItem: LiveData<Entry> = _currentItem

    init {
        connect()
    }

    fun fetchDataFromSharedViewModel(sharedViewModel: MainViewModel) {
        sharedViewModel.selectedItem.value?.let { item ->
            _currentItem.value = item  // SpeechViewModel 내부 LiveData에 저장
            DWLog.d("SpeechViewModel: sharedViewModel에서 데이터 가져옴 -> $item")
        }
    }

    fun finishMotion() {
        _speechStatus.value = SpeechStatus.END
        synchronized(this) {
            BaseRobotController.robotService?.robotMotor?.reset()
            BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.CALL_ACCEPT, callback)
            GCTextToSpeech.getInstance()?.speech(context.getString(R.string.message_finish_activity_recognition_1))
        }
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
        GCTextToSpeech.getInstance()?.setCallback(this)
        GCTextToSpeech.getInstance()?.start(context)
        RxBus.publish(RxEvent.destroyLongAppUpdate60)
    }

    fun disconnect() {
        DWLog.d("disconnect")
        GCTextToSpeech.getInstance()?.release()
        WMediaPlayer.instance.setListener(null)
        RxBus.publish(RxEvent.removeNavigateToMenuFragment)
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

    override fun onVoiceResult(result: String?) {}

    fun speech(text: String) {
        GCTextToSpeech.getInstance()?.speech(text)
    }

    /**
     * 음성 입력 처리 콜백
     */
    private val voiceCallback = object : VoiceRecorder.Callback() {
        override fun onVoiceStart() {
            super.onVoiceStart()
            doStartVoice()
        }

        override fun onVoiceEnd() {
            super.onVoiceEnd()
            doEndVoice()
        }

        override fun onVoice(data: ByteArray?, size: Int) {
            super.onVoice(data, size)
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
        recordStatus = RecordStatus.RECORDING
        startVoiceRecorder()
    }

    fun stopRecording() {
        RxBus.publish(RxEvent.destroyLongAppUpdate90)
        RxBus.publish(RxEvent.navigateMenuFragment60)
        _isRecording.value = false // 녹음 종료
        recordStatus = RecordStatus.STOP
        stopVoiceRecorder()
    }

    fun playWavFile() {
        // 이미 재생 중이면 재시작 방지
        if (playStatus == PlayStatus.PLAY) return

        // 일시정지 상태라면 resume
        if (playStatus == PlayStatus.PAUSE) {
            resumeWavFile()
            return
        }

        stopWavFile() // 기존 재생 중이면 정지

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource("$wavDirPath$wavFileName$wavExt")
                prepare()
                start()
                playStatus = PlayStatus.PLAY

                DWLog.d("WAV 파일 재생 시작: $wavDirPath$wavFileName$wavExt")

                setOnCompletionListener {
                    onPlayCompleted?.invoke()
                    stopWavFile()
                }
            } catch (e: IOException) {
                DWLog.e("WAV 파일 재생 실패: ${e.message}")
            }
        }
    }

    fun pauseWavFile() {
        mediaPlayer?.takeIf { it.isPlaying }?.apply {
            pause()
            playStatus = PlayStatus.PAUSE
            DWLog.d("WAV 파일 일시정지")
        }
    }


    fun resumeWavFile() {
        mediaPlayer?.apply {
            start()
            playStatus = PlayStatus.PLAY
            DWLog.d("WAV 파일 재개")
        }
    }

    fun stopWavFile() {
        mediaPlayer?.apply {
            stop()
            DWLog.d("WAV 파일 정지")
        }
        releaseMediaPlayer()
        playStatus = PlayStatus.STOP
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopWavFile() // ViewModel 종료 시 정리
    }

    /**
     * 자서전 로그 저장
     * */
    private val _insertLogEvent = MutableLiveData<Status>()
    val insertLogEvent: LiveData<Status> get() = _insertLogEvent
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
                        Constant.YES
                    ),
                    wavUtils.getMultipartWaveFile()
                ).let { response ->
                    when (response.statusCode) {
                        -99 -> _insertLogEvent.postValue(Status(response.statusCode, response.message))
                        -3 -> _insertLogEvent.postValue(Status(response.statusCode, context.getString(R.string.message_not_registration_elderly)))
                        0 -> _insertLogEvent.postValue(Status(response.statusCode, response.message ?: ""))
                        else -> _insertLogEvent.postValue(Status(response.statusCode, response.message ?: ""))
                    }
                }
            } else {
                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                RxBus.publish(RxEvent.destroyApp)
            }
        }
    }

    var callback: IMotionCallback = object : IMotionCallback.Stub() {
        override fun finishMotion() {
            BaseRobotController.robotService?.robotMotor?.reset()
        }
    }

    enum class RecordStatus {
        INIT,
        RECORDING,
        STOP
    }

    enum class PlayStatus {
        INIT,
        PLAY,
        REPLAY,
        PAUSE,
        STOP
    }

    var onPlayCompleted: (() -> Unit)? = null

    companion object {
        private const val wavFileName = "recorded_bio_reply"
        private const val wavDirPath = "/sdcard/audio/temp/"
        private const val wavExt = ".wav"
    }
}