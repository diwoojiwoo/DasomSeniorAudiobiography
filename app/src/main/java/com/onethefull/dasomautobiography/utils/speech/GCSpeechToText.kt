package com.onethefull.dasomautobiography.utils.speech

import com.onethefull.dasomautobiography.utils.record.WavFileUitls

/**
 * Created by Douner on 2020/06/03.
 */
interface GCSpeechToText {
    fun start()
    fun pause()
    fun resume()
    fun release()
    fun onSuccess()
    fun setCallback(callback: SpeechToTextCallback)
    fun setWavUtils(wavFileUitls: WavFileUitls)


    interface SpeechToTextCallback {
        fun onSTTConnected()
        fun onSTTDisconneted()
        fun onVoiceStart()
        fun onVoice(data: ByteArray?, size: Int)
        fun onVoiceEnd()
        fun onVoiceResult(result: String?)
    }
}