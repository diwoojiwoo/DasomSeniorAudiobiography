package com.onethefull.dasomautobiography.utils.speech

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.BuildConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.Single
import java.lang.Exception


/**
 * Created by Douner on 2019. 4. 30..
 */

class GCTextToSpeech {

    private val disposables = CompositeDisposable()
    private val receiverMessenger = Messenger(CallbackHandler())
    private var callback: Callback? = null

    interface Callback {
        fun onSpeechStart()
        fun onSpeechFinish()

        fun onGenieSTTResult(result: String)
    }

    fun setCallback(callback: Callback) {
        DWLog.i("setCallback :: $callback ")
        this.callback = callback
    }


    //텍스트 TTS 요청
    private fun requestTextToSpeech(speechData: SpeechData) {
        DWLog.e("requestTextToSpeech $speechData")

        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, speechData.msg, 0, 0)
                .apply {
                    data = Bundle().apply { putString(MSG_BUNDLE_INPUT_TEXT, speechData.text) }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    private fun requestUrlMediaSpeech(url: String) {
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_PLAY_MEDIA_URL, 0, 0)
                .apply {
                    data = Bundle().apply { putString(MSG_BUNDLE_INPUT_URL, url) }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    //오프라인 오디오 재생
    private fun requestOfflineTextToSpeech(index: Int) {
        DWLog.e("requestOfflineTextToSpeech $index")
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_SPEECH_TO_OFFLINE, 0, 0)
                .apply {
                    data = Bundle().apply { putInt(MSG_BUNDLE_INPUT_INDEX, index) }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    //텍스트 TTS 요청 및 actionName 전달
    private fun requestTextToSpeech(speechData: SpeechData, actionName: String) {
//        DWLog.e("requestTextToSpeech $speechData, actionName $actionName")

        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, speechData.msg, 0, 0)
                .apply {
                    data = Bundle().apply {
                        putString(MSG_BUNDLE_INPUT_TEXT, speechData.text)
                        putString(MSG_BUNDLE_INPUT_ACTION_NAME, actionName)
                    }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Request Text to Speech ")
            e.printStackTrace()
        }
    }

    fun requestReleaseSpeech() {
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_SPEECH_RELEASE, 0, 0)
                .apply {
                    data = Bundle()
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    private fun requestStreamMusic(index: Int) {
        DWLog.e("requestStreamMusic $index")
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_MUSIC_TO_OFFLINE, 0, 0)
                .apply {
                    data = Bundle().apply { putInt(MSG_BUNDLE_INPUT_INDEX, index) }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Request Music to Speech ")
            e.printStackTrace()
        }
    }

    fun <T> T.excuete(f: T.() -> Unit) {
        f()
    }

    fun onError(t: Throwable) {
        t.printStackTrace()
        DWLog.w("GCTextToSpeech ==> onError :: ${t.message}")
    }

    @SuppressLint("CheckResult")
    fun speech(text: String) {
//        speech(text, MSG_GENIE_SPEECH_TO_TEXT)
        speech(text, MSG_SPEECH_TO_TEXT)
    }

    @SuppressLint("CheckResult")
    fun speech(text: String, actionName: String) {
//        speech(text, MSG_GENIE_SPEECH_TO_TEXT)
        speech(text, actionName, MSG_SPEECH_TO_TEXT)
    }

    @SuppressLint("CheckResult")
    fun speech(text: String, msg: Int) {
        DWLog.w("GCTextToSpeech ==> speech :: $text")
        call(text, msg)
            .subscribe(
                { speechData -> requestTextToSpeech(speechData) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    @SuppressLint("CheckResult")
    fun speech(text: String, actionName: String, msg: Int) {
        DWLog.w("GCTextToSpeech ==> speech :: $text, actionName :: $actionName")
        call(text, msg)
            .subscribe(
                { speechData -> requestTextToSpeech(speechData, actionName) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }


    // TODO REMOVE GENIE STT TEST : INIT
    fun initGenieSTT() {
        DWLog.e("GCTextToSpeech initGenieSTT")
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_GENIE_STT_INIT, 0, 0)
                .apply {
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    // TODO REMOVE GENIE STT TEST : START
    fun startGenieSTT() {
        DWLog.e("GCTextToSpeech StartGenieSTT")
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_GENIE_STT_START, 0, 0)
                .apply {
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    // TODO REMOVE GENIE STT TEST : END
    fun stopGenieSTT() {
//        DWLog.e("GCTextToSpeech stopGenieSTT")
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_GENIE_STT_STOP, 0, 0)
                .apply {
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    // TODO REMOVE GENIE STT TEST : SEND VOICE DATA
    fun sendVoiceData(data: ByteArray) {
//        DWLog.e("GCTextToSpeech sendVoiceData")
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_GENIE_STT_ON_VOICE, 0, 0)
                .apply {
                    this.data = Bundle().apply { putByteArray(KEY_GENIE_VOICE_DATA, data) }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    // TODO REMOVE GENIE STT TEST : GENIE_CONVERSATION
    fun requestGenieConv(text: String): Boolean {
        DWLog.e("GCTextToSpeech requestGenieConv [$mBound]")
        if (!mBound) return false
        mService?.send(Message.obtain(null, MSG_GENIE_REQUEST_GENIE_CONV, 0, 0)
            .apply {
                this.data = Bundle().apply { putString(KEY_TEXT_COMMAND_TEXT, text) }
                replyTo = receiverMessenger
            })
        return true
    }

    fun urlMediaSpeech(url: String) {
        DWLog.w("GCTextToSpeech ==> urlMediaSpeech :: $url [callback : $callback]")
        call(url)
            .subscribe(
                { url -> requestUrlMediaSpeech(url) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    fun offlineSpeech(index: Int) {
        DWLog.w("GCTextToSpeech ==> offlineSpeech :: $index [callback : $callback]")
        call(index)
            .subscribe(
                { index -> requestOfflineTextToSpeech(index) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    fun offlinePlayAudio(index: Int) {
        call(index)
            .subscribe(
                { index -> requestStreamMusic(index) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    private fun call(index: Int): Single<Int> {
        return Single.just(index)
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun call(url: String): Single<String> {
        return Single.just(url)
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun call(text: String, msg: Int): Single<SpeechData> {
        return Single.just(SpeechData(text, msg))
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    fun release() {
        requestReleaseSpeech()
        callback = null
        if (mBound) {
            context?.unbindService(mServiceConnection)
        }
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        instance = null
    }

    private var context: Context? = null


    //TODO EVENT BUS로 대채하기
    // startReceiver
    interface StartReceiver {
        fun onStart()
    }

    private var startReceiver: StartReceiver? = null

    fun start(context: Context, startReceiver: StartReceiver?) {
        DWLog.i("GCTextToSpeech Start!!")
        this.startReceiver = startReceiver
        this.context = context
        val intent = Intent().apply {
            component = ComponentName(GC_TTS_SERVICE_PACKAGENAME, GC_TTS_SERVICE_SERVICENAME)
        }
        try {
            this.context?.startService(intent)
            this.context?.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            DWLog.e("GCTextToSpeech Start!! Exception ${e.message}")
            e.printStackTrace()
        }
    }

    fun start(context: Context) {
        start(context, null)
    }

    private var mService: Messenger? = null
    var mBound = false

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            DWLog.i("onServiceDisconnected $name")
            mBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            DWLog.i("onServiceConnected $name")
            mService = Messenger(service)
            startReceiver?.onStart()
            mBound = true

            // TODO REMOVE GENIE STT TEST : INIT
            if (BuildConfig.PRODUCT_TYPE == "KT") initGenieSTT()
        }
    }

    data class SpeechData(val text: String, val msg: Int)

    inner class CallbackHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
//            DWLog.i("GCTextToSpeech::CallbackHandler:[${msg.what}]${msg.replyTo}")
            when (msg?.what) {
                MSG_SPEECH_START -> {
//                    DWLog.i("GCTextToSpeech MSG_SPEECH_START")
                    callback?.onSpeechStart()
                }

                MSG_SPEECH_END, MSG_SPEECH_SINGLE_END -> {
//                    DWLog.i("GCTextToSpeech MSG_SPEECH_END")
                    callback?.onSpeechFinish()
                }

                MSG_REQUEST_FAIL -> {
                    DWLog.e("GCTextToSpeech GCTextToSpeech MSG_SPEECH_END")
//                    SuspendTask.startSuspend(INDEX_OFFLINE_WIFI_IS_UNSTABLE)
                }

                MSG_GENIE_SPEECH_TO_TEXT -> {
//                    msg.data.get()
                }

                MSG_GENIE_STT_RESULT -> {

                    msg.data.getString(
                        MSG_SPEECH_TO_TEXT_BUNDLE_PARAM
                    )?.let {
                        DWLog.i("GCTextToSpeech MSG_GENIE_STT_RESULT  $it")
                        callback?.onGenieSTTResult(it)
                    }
                }
            }
            super.handleMessage(msg)
        }
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GCTextToSpeech? = null

        fun getInstance(): GCTextToSpeech? {
            if (null == instance) {
                synchronized(GCTextToSpeech::class.java) {
                    if (null == instance) {
                        instance = GCTextToSpeech()
                    }
                }
            }
            return instance
        }

        const val MSG_SPEECH_TO_OFFLINE = 0x9003
        const val MSG_SPEECH_TO_TEXT = 0x9001
        const val MSG_SPEECH_RELEASE = 0x9002
        const val MSG_GENIE_SPEECH_TO_TEXT = 0x9004
        const val MSG_GENIE_SPEECH_TO_RELEASE = 0x9005
        const val MSG_SPEECH_TO_TEXT_CHAT = 0x9006
        const val MSG_SPEECH_TO_OFFLINE_CLOVA = 0x9008
        const val MSG_PLAY_MEDIA_URL = 0x9011

        const val MSG_GENIE_STT_INIT = 0x5000
        const val MSG_GENIE_STT_START = 0x5001
        const val MSG_GENIE_STT_STOP = 0x5002
        const val MSG_GENIE_STT_RESULT = 0x5003
        const val MSG_GENIE_STT_ON_VOICE = 0x5004
        const val MSG_GENIE_REQUEST_GENIE_CONV = 0x5006

        const val MSG_SPEECH_TO_TEXT_BUNDLE_PARAM = "MSG_GENIE_STT_RESULT_PARAM"
        const val KEY_GENIE_VOICE_DATA = "KEY_VOICE_DATA"
        const val KEY_TEXT_COMMAND_TEXT = "KEY_TEXT_COMMAND_TEXT"


        private const val MSG_BUNDLE_INPUT_TEXT = "MSG_BUNDLE_INPUT_TEXT"
        private const val MSG_BUNDLE_INPUT_URL = "MSG_BUNDLE_INPUT_URL"
        private const val MSG_BUNDLE_INPUT_INDEX = "MSG_BUNDLE_INPUT_INDEX"
        private const val MSG_BUNDLE_INPUT_ACTION_NAME = "MSG_BUNDLE_INPUT_ACTION_NAME"

        private const val MSG_MUSIC_TO_OFFLINE = 0x4001

        const val INDEX_OFFLINE_BUSY_COMMNET = 1
        const val INDEX_OFFLINE_STUDY_COMMNET = 2
        const val INDEX_OFFLINE_REGIST_COMMNET = 3
        const val INDEX_OFFLINE_WIFI_COMMNET = 4
        const val INDEX_OFFLINE_UNKNOWN_RETRY_ = 5
        const val INDEX_OFFLINE_ALREADY_ALARM = 6
        const val INDEX_OFFLINE_SUSPEND_COMMENT = 7
        const val INDEX_OFFLINE_REQUEST_YOUTUBE_COMMENT = 8
        const val INDEX_OFFLINE_LIMIT_PHOTO_COMMENT = 9
        const val INDEX_OFFLINE_LIMIT_ASR_COMMENT = 10
        const val INDEX_OFFLINE_LIMIT_MONITERING_COMMENT = 11
        const val INDEX_OFFLINE_INVITE_RANDOM_CHAT_COMMENT = 12
        const val INDEX_OFFLINE_CHAT_START_INTRO = 13
        const val INDEX_OFFLINE_CHAT_WATING = 14
        const val INDEX_OFFLINE_CHAT_DO_SPEECH = 15
        const val INDEX_OFFLINE_START_SPEECH = 16
        const val INDEX_OFFLINE_WIFI_IS_UNSTABLE = 17
        const val INDEX_OFFLINE_REQUEST_SOS_COMMENT = 18

        // 랜덤채팅 홍보
        const val INDEX_OFFLINE_RANDOM_CHAT_PROMO_01 = 1901
        const val INDEX_OFFLINE_RANDOM_CHAT_PROMO_02 = 1902
        const val INDEX_OFFLINE_RANDOM_CHAT_PROMO_03 = 1903
        const val INDEX_OFFLINE_RANDOM_CHAT_PROMO_04 = 1904
        const val INDEX_OFFLINE_RANDOM_CHAT_RECOMM_01 = 1911
        const val INDEX_OFFLINE_RANDOM_CHAT_RECOMM_02 = 1912

        // 랜덤채팅 사전 알림음
        const val INDEX_OFFLINE_ALARM_SOUND_01 = 401

        private const val MSG_SPEECH_START = 0x8005
        private const val MSG_SPEECH_END = 0x8006
        private const val MSG_SPEECH_SINGLE_END = 0x8016

        private const val MSG_REQUEST_FAIL = -0x0001


        private const val GC_TTS_SERVICE_PACKAGENAME = "com.onethefull.gcspeechservice"
        private const val GC_TTS_SERVICE_SERVICENAME =
            "com.onethefull.gcspeechservice.GCSpeechService"


    }
}