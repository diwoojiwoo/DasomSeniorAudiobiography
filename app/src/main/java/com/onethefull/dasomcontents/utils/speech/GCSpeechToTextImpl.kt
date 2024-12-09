package com.onethefull.dasomcontents.utils.speech

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import com.google.cloud.android.speech.IRemoteService
import com.google.cloud.android.speech.IRemoteServiceCallback
import com.onethefull.dasomcontents.utils.logger.DWLog
import com.onethefull.dasomcontents.utils.record.VoiceRecorder
import com.onethefull.dasomcontents.utils.record.WavFileUitls
import org.jetbrains.annotations.Nullable

/**
 * Created by Douner on 2020/06/02.
 */
class GCSpeechToTextImpl(private val context: Activity) : GCSpeechToText {

    @Nullable
    private var mVoiceRecorder: VoiceRecorder? = null

    @Nullable
    private var wavUtils: WavFileUitls? = null

    @Nullable
    private var mSTTCallback: GCSpeechToText.SpeechToTextCallback? = null

    private var isSuccessRecog = false

    @Volatile
    private var isPauseOnVoice = false


    override fun start() {
        startServiceBind()
    }

    override fun pause() {
        isPauseOnVoice = true
        mVoiceRecorder?.pasue()
    }

    override fun resume() {
        isPauseOnVoice = false
        mVoiceRecorder?.resume()
    }

    override fun release() {
        stopServiceBind()
    }

    override fun onSuccess() {
    }

    override fun setWavUtils(wavFileUitls: WavFileUitls) {
        this.wavUtils = wavFileUitls
    }


    override fun setCallback(callback: GCSpeechToText.SpeechToTextCallback) {
        this.mSTTCallback = callback
    }

    /**
     * 서비스 시작 핸들러
     */
    private val startService = Runnable {
        DWLog.d("Start Speech Service")
        val intent = Intent().apply {
            component = ComponentName(
                SERVICE_APP_PACKAGE,
                SERVICE_APP_CLASS
            )
        }
        context.startService(intent)
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)

    }

    /**
     * 서비스 연결 리스너
     */
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            DWLog.d("onServiceDisconnected ==> $name")
            if (mService != null) {
                try {
                    mService?.unregisterCallback(mRemoteCallback)
                    mSTTCallback?.onSTTDisconneted()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            DWLog.d("onServiceConnected ==> $name")
            mSTTCallback?.onSTTConnected()
            service?.let {
                try {
                    mService = IRemoteService.Stub.asInterface(it).apply {
                        registerCallback(mRemoteCallback)
                    }
                    startVoiceRecorder()
                    mSTTCallback?.onSTTConnected()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    DWLog.e("onServiceConnected ==> ${e.message}")
                }
            }
        }
    }

    private var mService: IRemoteService? = null

    /**
     * 구글 음성인식 서비스 인식결과 콜백
     */
    private var mRemoteCallback: IRemoteServiceCallback = object : IRemoteServiceCallback.Stub() {
        override fun onSpeechRecognized(text: String?) {
            if (isSuccessRecog) return
            isSuccessRecog = true
            wavUtils?.finish()
            mVoiceRecorder?.pasue()
            mSTTCallback?.onVoiceResult(text)
        }
    }

    /**
     * 음성입력 처리 콜백
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
     * Recording 데이터 처리
     */
    private fun doOnVoice(data: ByteArray?, size: Int) {
        mSTTCallback?.onVoice(data, size)
        if (mService != null) {
            try {
                if (isPauseOnVoice) return
                mService?.recognize(data, size)
                wavUtils?.writeAudioDataToFile(data)
            } catch (e: RemoteException) {
                stopVoiceRecorder()
            } catch (e1: NullPointerException) {
                e1.printStackTrace()
            } catch (e2: IllegalStateException) {
                e2.printStackTrace()
            }
        }
    }

    /**
     * Recording 종료
     */
    private fun doEndVoice() {
        mSTTCallback?.onVoiceEnd()
        if (mService != null) {
            try {
                mService?.finishRecognizing()
                if (!isSuccessRecog) wavUtils?.finish()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Recording 시작
     */
    private fun doStartVoice() {
        mSTTCallback?.onVoiceStart()
        if (mService != null) {
            try {
                isSuccessRecog = false
                if (mVoiceRecorder != null) {
                    mVoiceRecorder?.sampleRate?.let { mService?.startRecognizing(it) }
                    wavUtils?.startWavStream()
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 음성 레코드 시작
     */
    private fun startVoiceRecorder() {
        DWLog.d("VoiceRecorder::startVoiceRecorder")
        if (mVoiceRecorder != null) {
            mVoiceRecorder?.stop()
        }
        mVoiceRecorder = VoiceRecorder(voiceCallback)
        try {
            DWLog.d("VoiceRecorder::startVoiceRecorder $mVoiceRecorder")
            mVoiceRecorder?.start()
        } catch (e: Exception) {
            DWLog.e("VoiceRecorder::Exception::" + e.message)
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

    /**
     * 음성인식 서비스 바인드
     */
    private fun startServiceBind() {
        DWLog.d("startServiceBind")
        try {
            Handler().post(startService)
        } catch (e: Exception) {
            e.printStackTrace()
            DWLog.e("startServiceBind :: Exception ==> " + e.message)
            try {
                stopServiceBind()
            } catch (e2: NullPointerException) {
                DWLog.e("startServiceBind :: Exception [Destory] ==> " + e.message)
                e.printStackTrace()
            }
        }
    }

    /**
     * 음성인식 서비스 정지
     */
    private fun stopServiceBind() {
        try {
            stopVoiceRecorder()
            mService?.unregisterCallback(mRemoteCallback)
            context.unbindService(mServiceConnection)
        } catch (e: RemoteException) {
            e.printStackTrace()
        } catch (e2: IllegalArgumentException) {
            e2.printStackTrace()
        }

    }

    companion object {
        const val ERROR_OUT_OF_RANGE = "RVJST1JfT1VUX09GX1JBTkdF"
        private const val SERVICE_APP_CLASS = "com.google.cloud.android.speech.SpeechService"
        private const val SERVICE_APP_PACKAGE = "com.google.cloud.android.speech"
        private const val TIME_APP_TERMINATE = 90 * 1000L
    }


}