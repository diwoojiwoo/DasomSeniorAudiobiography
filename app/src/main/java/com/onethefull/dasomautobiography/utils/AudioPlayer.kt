import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var isPaused = false

    fun playAudio(filePath: String) {
        if (isPaused) {
            mediaPlayer?.start()  // 일시 정지 상태라면 재개
            isPaused = false
            Log.d("AudioPlayer", "오디오 재개")
            return
        }

        stopAudio() // 기존 재생 중인 오디오 중지

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath) // 파일 경로 설정
                prepare() // 준비
                start() // 재생
                Log.d("AudioPlayer", "오디오 재생 시작")
            } catch (e: IOException) {
                Log.e("AudioPlayer", "오디오 재생 실패: ${e.message}")
            }

            setOnCompletionListener {
                Log.d("AudioPlayer", "오디오 재생 완료")
                releaseMediaPlayer()
            }
        }
    }

    fun pauseAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                isPaused = true
                Log.d("AudioPlayer", "오디오 일시 정지")
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying || isPaused) {
                stop()
            }
            releaseMediaPlayer()
            Log.d("AudioPlayer", "오디오 정지")
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPaused = false
    }
}
