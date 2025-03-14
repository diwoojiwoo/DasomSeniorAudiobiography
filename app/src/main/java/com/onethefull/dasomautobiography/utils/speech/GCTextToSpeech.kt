package com.onethefull.dasomautobiography.utils.speech

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.BuildConfig
import com.onethefull.dasomautobiography.utils.Product
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.Single
import java.lang.Exception
import java.util.*


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
        fun onFinishService()
        fun onWonderfulRequest(result: String)
        fun onRequestHardwareControl(result: String)
        fun onGenieSTTResult(result: String)
        fun onGenieSTTResultNoAction(result: String)
        fun onGenieCommandStart(result: String)
        fun requestSendGenieLog(sttResult: String, genieResponse: String)
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

    // GENIE MIDIA
    private fun requestUrlGenieMediaSpeech(url: String) {
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_PLAY_MEDIA_GENIE_URL, 0, 0)
                .apply {
                    data = Bundle().apply { putString(MSG_BUNDLE_INPUT_URL, url) }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }

    //오프라인 오디오 재생 영어
    private fun requestOfflineTextToSpeechEn(index: Int) {
        requestOfflineTextToSpeechEn(index, null)
    }


    //오프라인 오디오 재생
    private fun requestOfflineTextToSpeechEn(index: Int, actionName: String?) {
        DWLog.e("requestOfflineTextToSpeech $index [$actionName]")
        if (!mBound) return
        try {
            mService?.send(Message.obtain(null, MSG_SPEECH_TO_OFFLINE_ENGLISH, 0, 0)
                .apply {
                    data = Bundle().apply {
                        putInt(MSG_BUNDLE_INPUT_INDEX, index)
                        actionName?.let {
                            putString(MSG_BUNDLE_INPUT_ACTION_NAME, actionName)
                        }
                    }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }


    //오프라인 오디오 재생
    private fun requestOfflineTextToSpeech(index: Int) {
        requestOfflineTextToSpeech(index, null)
    }


    //오프라인 오디오 재생
    private fun requestOfflineTextToSpeech(index: Int, actionName: String?) {
        DWLog.e("requestOfflineTextToSpeech $index [$actionName]")
        if (!mBound) return
        try {
            DWLog.e("requestOfflineTextToSpeech $index MSG_SPEECH_TO_OFFLINE")
            mService?.send(Message.obtain(null, MSG_SPEECH_TO_OFFLINE, 0, 0)
                .apply {
                    data = Bundle().apply {
                        putInt(MSG_BUNDLE_INPUT_INDEX, index)
                        actionName?.let {
                            putString(MSG_BUNDLE_INPUT_ACTION_NAME, actionName)
                        }
                    }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            DWLog.e("RemoteException :: Service Requet Text to Speech ")
            e.printStackTrace()
        }
    }


    private fun requestPlayEffectSound(index: Int, isWithoutCallback: Boolean = false) {
        if (!mBound) return
        try {
            mService?.send(Message.obtain(
                null,
                if (isWithoutCallback) MSG_PLAY_SOUND_EFFECT_WITHOUT_CALLBACK
                else MSG_PLAY_SOUND_EFFECT, 0, 0
            )
                .apply {
                    data = Bundle().apply { putInt(MSG_BUNDLE_INPUT_INDEX, index) }
                    replyTo = receiverMessenger
                })
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    //텍스트 TTS 요청 및 actionName 전달
    private fun requestTextToSpeech(speechData: SpeechData, actionName: String) {
        DWLog.e("requestTextToSpeech $speechData, actionName $actionName")

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
        DWLog.e("requestStreamMusic $index [$mBound]")
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


    fun speechTest(text: String) {
        speech(text, MSG_SPEECH_TO_TEXT_GCTTS)
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


    // GENIE_CONVERSATION Init
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


//    // GENIE_CONVERSATION 기가지니 인사이드 주소 등록
//    private fun setGenieUserLocation(locationData: LocationData): Boolean {
//
//        DWLog.e("GCTextToSpeech requestGenieConv [$mBound] $locationData")
//        if (!mBound) return false
//        mService?.send(Message.obtain(null, MSG_GENIE_SET_LOCATION, 0, 0)
//            .apply {
//                this.data = Bundle().apply {
//                    putString(
//                        KEY_GENIE_LOCATION_DATA,
//                        Gson().toJson(locationData).toString()
//                    )
//                }
//                replyTo = receiverMessenger
//            })
//        return true
//    }


    // GENIE_CONVERSATION Start STT
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

    // GENIE_CONVERSATION Stop STT
    fun stopGenieSTT() {
        DWLog.e("GCTextToSpeech stopGenieSTT")
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

    // GENIE_CONVERSATION STT 데이터 입력
    fun sendVoiceData(data: ByteArray) {
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

    // GENIE_CONVERSATION 대화요청
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

    // GENIE_CONVERSATION 텍스트 기반 대화 요청
    fun requestGenieCommandOnly(text: String): Boolean {
        DWLog.e("GCTextToSpeech requestGenieConv [$mBound]")
        if (!mBound) return false
        mService?.send(Message.obtain(null, MSG_GENIE_REQUEST_GENIE_CONV_TEXT_ONLY, 0, 0)
            .apply {
                this.data = Bundle().apply { putString(KEY_TEXT_COMMAND_TEXT, text) }
                replyTo = receiverMessenger
            })
        return true
    }

    // GENIE_CONVERSATION url 미디어 재생
    fun urlMediaSpeech(url: String) {
        DWLog.w("GCTextToSpeech ==> urlMediaSpeech :: $url [callback : $callback]")
        call(url)
            .subscribe(
                { url -> requestUrlMediaSpeech(url) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    // GENIE_CONVERSATION url 기가지니 인사이드 TTS 재생
    fun urlGenieMediaSpeech(url: String) {
        DWLog.w("GCTextToSpeech ==> urlGenieMediaSpeech :: $url [callback : $callback]")
        call(url)
            .subscribe(
                { url -> requestUrlGenieMediaSpeech(url) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    // Offline Speech 재생
    fun offlineSpeech(index: Int) {
        DWLog.w("GCTextToSpeech ==> offlineSpeech :: $index [callback : $callback]")
        call(index)
            .subscribe(
                { index ->
                    if (App.instance.defaultLanguage != Locale.US) requestOfflineTextToSpeech(index)
                    else requestOfflineTextToSpeechEn(index)
                },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }


    // Offline Speech 재생 + actionName 추가
    fun offlineSpeech(index: Int, actionName: String) {
        DWLog.w("GCTextToSpeech ==> offlineSpeech :: $index [callback : $callback]")
        call(index)
            .subscribe(
                { index ->
                    if (App.instance.defaultLanguage != Locale.US) requestOfflineTextToSpeech(
                        index,
                        actionName
                    )
                    else requestOfflineTextToSpeechEn(index, actionName)
                },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    fun startEffectSound(index: Int) {
        call(index)
            .subscribe(
                { index -> requestPlayEffectSound(index) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }

    fun startEffectSoundWithoutCallback(index: Int) {
        call(index)
            .subscribe(
                { index -> requestPlayEffectSound(index, true) },
                { e -> onError(e) })
            .let { disposables.add(it) }
    }


    fun offlineSpeechEn(index: Int) {
        DWLog.w("GCTextToSpeech ==> offlineSpeech :: $index [callback : $callback]")
        call(index)
            .subscribe(
                { index -> requestOfflineTextToSpeechEn(index) },
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

        DWLog.i("GCTextToSpeech release")
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
            mBound = true
            // TODO REMOVE GENIE STT TEST : INIT
            if (BuildConfig.PRODUCT_TYPE == Product.PRODUCT_KT)
                initGenieSTT()
//            initUserAddress()
            startReceiver?.onStart()
        }
    }

    private fun checkPreSentence() {

    }

    // 기가지니 인사이드 - 보호자앱 지역 세팅
//    private fun initUserAddress() {
//        context?.let { context ->
//
//            // 대화앱 저장 사용자 주소
//            val spInfo = DasomSharedPreference.getInstance(context)?.getAddressInfo()
//
//            // MQTT 수신 주소
//            val providerInfo = DasomProviderHelper.getElderyInfoOriginData(context)
//
//            if (providerInfo.isNotEmpty()) {
//                if (spInfo != providerInfo) {
//                    DasomSharedPreference.getInstance(context)?.setAddressInfo(providerInfo)
//                    Handler().postDelayed({
//                        DasomProviderHelper.getElderlyInfo(context)?.let {
//                            setGenieUserLocation(
//                                LocationData(
//                                    it.LON.toString(),
//                                    it.LAT.toString(),
//                                    it.ELDERLY_ADDRESS.toString()
//                                )
//                            )
//                        }
//                    }, 2000)
//                } else {
//
//                }
//            } else {
//                if (!spInfo.isNullOrEmpty()) {
//                    DasomSharedPreference.getInstance(context)?.setAddressInfo(String())
//                } else {
//                    context.sendBroadcast(Intent(App.ACTION_ELDERLY_INFO_RELOAD))
//                }
//            }
//        }
//    }

    data class SpeechData(val text: String, val msg: Int)

    inner class CallbackHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            DWLog.i("ITLDVoiceTouch GCTextToSpeech::CallbackHandler:[${msg.what}]${msg.replyTo}")
            when (msg?.what) {
                MSG_SPEECH_START -> {
                    DWLog.i("GCTextToSpeech MSG_SPEECH_START")
                    callback?.onSpeechStart()
                }

                MSG_SPEECH_END -> {
                    DWLog.i("GCTextToSpeech MSG_SPEECH_END")
                    callback?.onSpeechFinish()
                }

                MSG_SPEECH_SINGLE_END -> {
                    DWLog.i("GCTextToSpeech MSG_SPEECH_SINGLE_END")
                    RxBus.publish(RxEvent.destroyApp)
//                    callback?.onFinishService()
                }

                MSG_REQUEST_FAIL -> {
//                    SuspendTask.startSuspend(INDEX_OFFLINE_WIFI_IS_UNSTABLE)
                }

                MSG_GENIE_SPEECH_TO_TEXT -> {
//                    msg.data.get()
                }

                MSG_GENIE_STT_RESULT -> {
                    msg.data.getString(
                        MSG_SPEECH_TO_TEXT_BUNDLE_PARAM
                    )?.let {
                        DWLog.i("MSG_GENIE_STT_RESULT  $it")
                        callback?.onGenieSTTResult(it)
                    }
                }

                // 기가지니 인사이드 STT 결과만 수신 별도 동작 없음
                MSG_GENIE_STT_RESULT_NO_ACTION -> {
                    msg.data.getString(
                        MSG_SPEECH_TO_TEXT_BUNDLE_PARAM
                    )?.let {
                        DWLog.i("MSG_GENIE_STT_RESULT_NO_ACTION  $it")
                        callback?.onGenieSTTResultNoAction(it)
                    }
                }

                // 기가지니 인사이드 STT 텍스트 결과 별도 처리
                MSG_GENIE_STT_RESULT_ONLY_TEXT -> {
                    msg.data.getString(
                        MSG_SPEECH_TO_TEXT_BUNDLE_PARAM
                    )?.let {
                        DWLog.i("MSG_GENIE_STT_RESULT_ONLY_TEXT  $it")
                        callback?.onWonderfulRequest(it)
                    }
                }

                // 기가지니 인사이드 하드웨어 이벤트 처리
                MSG_GENIE_RESULT_FOR_CONTENT_CONTROL -> {
                    msg.data.getString(
                        MSG_GENIE_CONTENT_CONTROL_RESULT
                    )?.let {
                        DWLog.i("MSG_GENIE_CONTENT_CONTROL_RESULT  $it")
                        callback?.onRequestHardwareControl(it)
                    }
                }

                // 기가지니 인사이드 이벤트 처리
                MSG_GENIE_ON_EVENT -> {
                    msg.data.getString(
                        MSG_GENIE_EVENT_PARAM
                    )?.let {
                        DWLog.i("MSG_GENIE_CONTROL_MEDIA_COMMAND  $it")
                        if (it == "finish")
                            RxBus.publish(
                                RxEvent.Event(
                                    RxEvent.AppDestroy,
                                    1000,
                                    "AppDestroy"
                                )
                            )
                        else {
                            callback?.onGenieCommandStart(it)
                        }
                    }
                }

                //
                MSG_GENIE_PLAY_MEDIA_RESULT -> {
                    msg.data.getString(
                        MSG_GENIE_CMD_OPT_RESULT
                    )?.let {
                        DWLog.i("MSG_GENIE_CMD_OPT_RESULT  $it")
                    }
                }

                // 기가지니 인사이드 로그 저장
                MSG_REQUEST_GENIE_SEND_LOG -> {
                    val sttResult = msg.data.getString(KEY_GENIE_SEND_LOG_PARAM_STT) ?: ""
                    val genieResponse = msg.data.getString(KEY_GENIE_SEND_LOG_PARAM_RESPONSE) ?: ""
                    DWLog.i("DO MSG_REQUEST_GENIE_SEND_LOG [$sttResult] $genieResponse")
                    if (sttResult.isNotEmpty() && genieResponse.isNotEmpty()) {
                        callback?.requestSendGenieLog(sttResult, genieResponse)
                    }
                }

                // 기가지니 주소 미등록
                MSG_GENIE_REQUEST_LOCATION_INFO -> {
//                    DasomSharedPreference.getInstance(App.instance)?.setAddressInfo("")
//                    initUserAddress()
                }
            }
            super.handleMessage(msg)
        }
    }

    //TEST
    @SuppressLint("CheckResult")
    fun speechAndNextAction(text: String, action: () -> Unit) {
//        speech(text, MSG_GENIE_SPEECH_TO_TEXT)
        speech(text, MSG_SPEECH_TO_TEXT)

        this.setCallback(object : Callback {
            override fun onSpeechStart() {
            }

            override fun onSpeechFinish() {
                action()
            }

            override fun onFinishService() {
            }

            override fun onWonderfulRequest(result: String) {
            }

            override fun onRequestHardwareControl(result: String) {
            }

            override fun onGenieSTTResult(result: String) {
            }

            override fun onGenieSTTResultNoAction(result: String) {
            }

            override fun onGenieCommandStart(result: String) {
            }

            override fun requestSendGenieLog(sttResult: String, genieResponse: String) {
            }

        })
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

        const val STATE_FINISH_WAITING = 0
        const val MSG_SPEECH_TO_TEXT = 0x9001
        const val MSG_SPEECH_RELEASE = 0x9002
        const val MSG_SPEECH_TO_OFFLINE = 0x9003
        const val MSG_GENIE_SPEECH_TO_TEXT = 0x9004
        const val MSG_GENIE_SPEECH_TO_RELEASE = 0x9005
        const val MSG_SPEECH_TO_TEXT_CHAT = 0x9006
        const val MSG_SPEECH_TO_TEXT_CHAT_CLOVA = 0x9007
        const val MSG_SPEECH_TO_OFFLINE_CLOVA = 0x9008
        const val MSG_SPEECH_TO_TEXT_CLOVA = 0x9009
        const val MSG_PLAY_MEDIA_URL = 0x9011
        const val MSG_PLAY_MEDIA_GENIE_URL = 0x9012
        const val MSG_SPEECH_TO_OFFLINE_ENGLISH = 0x9013
        const val MSG_SPEECH_TO_TEXT_GCTTS = 0x9014
        const val MSG_PLAY_SOUND_EFFECT = 0x9051
        const val MSG_PLAY_SOUND_EFFECT_WITHOUT_CALLBACK = 0x9052

        const val MSG_GENIE_STT_INIT = 0x5000
        const val MSG_GENIE_STT_START = 0x5001
        const val MSG_GENIE_STT_STOP = 0x5002
        const val MSG_GENIE_STT_RESULT = 0x5003
        const val MSG_GENIE_STT_ON_VOICE = 0x5004
        const val MSG_GENIE_REQUEST_GENIE_CONV = 0x5006
        const val MSG_GENIE_REQUEST_GENIE_CONV_TEXT_ONLY = 0x5007
        const val MSG_GENIE_PLAY_MEDIA_RESULT = 0x5008
        const val MSG_GENIE_STT_RESULT_ONLY_TEXT = 0x5009
        const val MSG_GENIE_RESULT_FOR_CONTENT_CONTROL = 0x5014
        const val MSG_GENIE_ON_EVENT = 0x5010
        const val MSG_GENIE_STT_RESULT_NO_ACTION = 0x5011
        const val MSG_GENIE_SET_LOCATION = 0x5101 // 기가지니 인사이드 주소 등록
        const val MSG_GENIE_REQUEST_LOCATION_INFO = 0x5019 // 기가지니 인사이드 주소 미등록

        const val MSG_GENIE_CMD_OPT_RESULT = "MSG_GENIE_CMD_OPT_RESULT"
        const val MSG_GENIE_CONTENT_CONTROL_RESULT = "MSG_GENIE_CONTENT_CONTROL_RESULT"
        const val MSG_SPEECH_TO_TEXT_BUNDLE_PARAM = "MSG_GENIE_STT_RESULT_PARAM"
        const val MSG_GENIE_EVENT_PARAM = "MSG_GENIE_CONTROL_PARAM"
        const val KEY_GENIE_VOICE_DATA = "KEY_VOICE_DATA"
        const val KEY_TEXT_COMMAND_TEXT = "KEY_TEXT_COMMAND_TEXT"
        const val KEY_GENIE_LOCATION_DATA = "KEY_GENIE_LOCATION_DATA"


        const val KEY_GENIE_SEND_LOG_PARAM_STT = "KEY_GENIE_SEND_LOG_PARAM_STT"
        const val KEY_GENIE_SEND_LOG_PARAM_RESPONSE = "KEY_GENIE_SEND_LOG_PARAM_RESPONSE"


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
        const val INDEX_OFFLINE_REQUEST_STOP_CHAT_COMMENT = 19
        const val INDEX_OFFLINE_FINISH_QUIZ_COMMENT = 20

        const val INDEX_EFFECT_SOFT_SOFT_PIANO_01 = 40011
        const val INDEX_EFFECT_SOFT_SOFT_PIANO_02 = 40012
        const val INDEX_EFFECT_SOUND_BRIGHT_LOGO = 40013
        const val INDEX_EFFECT_SOUND_HARP_FLOURISH = 40014
        const val INDEX_EFFECT_SOFT_HAPPY_CHORD = 40015
        const val INDEX_EFFECT_SOUND_SHORT_CLICK_01 = 40016
        const val INDEX_EFFECT_OPENING_APP = 40101
        const val INDEX_EFFECT_LOADING_LONG = 40102
        const val INDEX_EFFECT_COMPLETE_CONVERSATION= 40103

        const val INDEX_EFFECT_BUBBLE_POP = 40201
        const val INDEX_EFFECT_CARTOON_JUMP = 40202
        const val INDEX_EFFECT_CHILD_HUM_UP = 40203
        const val INDEX_EFFECT_COMEDY_HICCUP_LOOP = 40204
        const val INDEX_EFFECT_COMPLETE_BELL = 40205
        const val INDEX_EFFECT_CONVERSATION_LOADING = 40206
        const val INDEX_EFFECT_ELECTRIC_POP = 40207
        const val INDEX_EFFECT_PASSING_PAGES = 40208
        const val INDEX_EFFECT_SAND_SWISH = 40209
        const val INDEX_EFFECT_SIGH_PITCH_UP = 40210
        const val INDEX_EFFECT_SLIDE_DOWN_AND_UP = 40211
        const val INDEX_EFFECT_SLIDE_WHISTLE_POP_UP = 40212
        const val INDEX_EFFECT_SLIDE_WHISTLE_UP_AND_DOWN_SLOW = 40213
        const val INDEX_EFFECT_SMALL_JUMP = 40214
        const val INDEX_EFFECT_SNEEZE_DOWN_SLOW = 40215
        const val INDEX_EFFECT_SURPRISE_GASP = 40216
        const val INDEX_EFFECT_WHISTLE_01 = 40217
        const val INDEX_EFFECT_WHISTLE_VARIOUS_1 = 40218
        const val INDEX_EFFECT_WHISTLE_VARIOUS_2 = 40219
        const val INDEX_EFFECT_WHISTLE_VARIOUS_3 = 40220



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
        private const val MSG_REQUEST_GENIE_SEND_LOG = 0x8051


        private const val GC_TTS_SERVICE_PACKAGENAME = "com.onethefull.gcspeechservice"
        private const val GC_TTS_SERVICE_SERVICENAME =
            "com.onethefull.gcspeechservice.GCSpeechService"


    }
}