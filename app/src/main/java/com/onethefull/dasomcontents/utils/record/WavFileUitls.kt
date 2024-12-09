package com.onethefull.dasomcontents.utils.record

import com.onethefull.dasomcontents.utils.logger.DWLog
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.*
import java.lang.NullPointerException

/**
 * Created by Douner on 2019. 4. 12..
 */
class WavFileUitls {

    companion object {
        private const val wavFileName = "recorded_audio"
        private const val wavTempFileName = "recorded_audio_temp"
        private const val wavTempFileName2 = "recorded_audio_temp2"
        private const val wavDirPath = "/sdcard/audio/temp/"
        private const val wavExt = ".wav"
        private const val pcmExt = ".pcm"

        private const val WAVE_CHANNEL_MONO = 1
        private const val HEADER_SIZE = 0x2c
        private const val RECORDER_BPP = 16

        private const val debug = false
    }

    enum class Type(var ext: String) {
        PCM(pcmExt), WAV(wavExt)
    }

    private var type = Type.WAV

    private var mBOStream: BufferedOutputStream? = null
    private var currentTempFilePath: String? = null
    private var _temp_position = 0

    init {
        initFile()
    }

    fun setType(type: Type) {
        this.type = type
    }

    /**
     * Init File
     */
    private fun initFile() {
        val dir = File(WavFileUitls.wavDirPath)
        val fileTemp = File("$wavDirPath$wavTempFileName${type.ext}")
        val fileTemp2 = File("$wavDirPath$wavTempFileName2${type.ext}")
        val fileNew = File("$wavDirPath$wavFileName${type.ext}")
        if (!dir.exists()) dir.mkdirs()
        if (fileTemp.exists()) fileTemp.delete()
        if (fileTemp2.exists()) fileTemp2.delete()
        if (fileNew.exists()) fileNew.delete()
    }

    private fun initStream() {
        val currentTempFileName =
            if (_temp_position == 0) wavTempFileName else wavTempFileName2
        currentTempFilePath = "$wavDirPath$currentTempFileName${type.ext}"
        var currentTempFile = File(currentTempFilePath)
        if (currentTempFile.exists()) {
            currentTempFile.delete()
        }
        mBOStream = BufferedOutputStream(FileOutputStream(currentTempFile))
    }

    @Synchronized
    fun startWavStream() {
        if (debug) DWLog.e("WavFileUitls ==> [startWavStream] ${type.ext}")
        initStream()
    }

    @Synchronized
    fun writeAudioDataToFile(audioData: ByteArray?) {
        if (debug) DWLog.e("WavFileUitls ==> [writeAudioDataToFile] ${audioData?.size}")
        try {
            mBOStream?.write(audioData)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun finish() {
        _temp_position = (_temp_position + 1) % 2
        mBOStream?.flush()
        mBOStream?.close()
        mBOStream = null
        saveWaveFile()
    }

    @Synchronized
    private fun saveWaveFile() {
        try {
            if (debug) DWLog.e("WavFileUitls ==> [start::saveWaveFile] $currentTempFilePath")

            val length = File(currentTempFilePath).length().toInt()
            var mBIStream = BufferedInputStream(FileInputStream(currentTempFilePath))
            var mBOStream =
                BufferedOutputStream(FileOutputStream("$wavDirPath$wavFileName${type.ext}"))

            if (type == Type.WAV) mBOStream.write(getFileHeader(length))

            val buffer = ByteArray(VoiceRecorder.BUFFER_SIZE)
            var read = mBIStream.read(buffer)
            while ((read) != -1) {
                mBOStream.write(buffer)
                read = mBIStream.read(buffer)
            }
            mBOStream.flush()
            mBIStream.close()
            mBOStream.close()
            if (debug) DWLog.e("WavFileUitls ==> [finish::saveWaveFile] $currentTempFilePath")
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }


    fun getMultipartWaveFile(): MultipartBody.Part {
        val recordFile = File("$wavDirPath$wavFileName${type.ext}")
        if (!recordFile.exists()) {
            recordFile.mkdirs()
            recordFile.createNewFile()
        }
        return MultipartBody.Part.createFormData(
            "FILE",
            recordFile.name,
            RequestBody.create(MediaType.parse("multipart/form-data"), recordFile)
        )
    }

    fun getEmptyMultipartWaveFile(): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            "FILE",
            "",
            RequestBody.create(MediaType.parse("multipart/form-data"), "")
        )
    }

    private fun getFileHeader(mAudioLen: Int): ByteArray {
        return ByteArray(HEADER_SIZE).apply {
            val totalDataLen = mAudioLen + 40
            val byteRate = RECORDER_BPP * VoiceRecorder.CURRENT_SAMPLE_RATE * WAVE_CHANNEL_MONO / 8
            this[0] = 'R'.toByte()  // RIFF/WAVE header
            this[1] = 'I'.toByte()
            this[2] = 'F'.toByte()
            this[3] = 'F'.toByte()
            this[4] = (totalDataLen and 0xff).toByte()
            this[5] = (totalDataLen shr 8 and 0xff).toByte()
            this[6] = (totalDataLen shr 16 and 0xff).toByte()
            this[7] = (totalDataLen shr 24 and 0xff).toByte()
            this[8] = 'W'.toByte()
            this[9] = 'A'.toByte()
            this[10] = 'V'.toByte()
            this[11] = 'E'.toByte()
            this[12] = 'f'.toByte()  // 'fmt ' chunk
            this[13] = 'm'.toByte()
            this[14] = 't'.toByte()
            this[15] = ' '.toByte()
            this[16] = 16  // 4 bytes: size of 'fmt ' chunk
            this[17] = 0
            this[18] = 0
            this[19] = 0
            this[20] = 1.toByte()  // format = 1 (PCM방식)
            this[21] = 0
            this[22] = WAVE_CHANNEL_MONO.toByte()
            this[23] = 0
            this[24] = (VoiceRecorder.CURRENT_SAMPLE_RATE and 0xff).toByte()
            this[25] = (VoiceRecorder.CURRENT_SAMPLE_RATE shr 8 and 0xff).toByte()
            this[26] = (VoiceRecorder.CURRENT_SAMPLE_RATE shr 16 and 0xff).toByte()
            this[27] = (VoiceRecorder.CURRENT_SAMPLE_RATE shr 24 and 0xff).toByte()
            this[28] = (byteRate and 0xff).toByte()
            this[29] = (byteRate shr 8 and 0xff).toByte()
            this[30] = (byteRate shr 16 and 0xff).toByte()
            this[31] = (byteRate shr 24 and 0xff).toByte()
            this[32] = (RECORDER_BPP * WAVE_CHANNEL_MONO / 8).toByte()  // block align
            this[33] = 0
            this[34] = RECORDER_BPP.toByte()  // bits per sample
            this[35] = 0
            this[36] = 'd'.toByte()
            this[37] = 'a'.toByte()
            this[38] = 't'.toByte()
            this[39] = 'a'.toByte()
            this[40] = (mAudioLen and 0xff).toByte()
            this[41] = (mAudioLen shr 8 and 0xff).toByte()
            this[42] = (mAudioLen shr 16 and 0xff).toByte()
            this[43] = (mAudioLen shr 24 and 0xff).toByte()
        }
    }
}
