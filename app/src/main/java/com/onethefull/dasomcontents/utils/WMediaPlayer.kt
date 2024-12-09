package com.onethefull.dasomcontents.utils

import android.media.MediaPlayer
import com.onethefull.dasomcontents.App
import com.onethefull.dasomcontents.utils.logger.DWLog


class WMediaPlayer private constructor() {
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var mListener: OnMediaPlayerListener? = null

    fun setListener(listener: OnMediaPlayerListener?) {
        mListener = listener
    }

    fun start(resourceId: Int) {
        DWLog.d("WMediaPlayer ==>  start $resourceId")
        mediaPlayer = MediaPlayer.create(App.instance.applicationContext, resourceId)
        mediaPlayer.start()
        mListener?.mediaPlayed()
        mediaPlayer.setOnCompletionListener { mp ->
            mListener?.mediaCompleted()
            stop()
        }
    }

    private fun stop() {
        DWLog.d("WMediaPlayer ==>  stop ")
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.setOnCompletionListener(null)
                mediaPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pause() {
        DWLog.d("WMediaPlayer ==>  pause ")
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }

    fun restart() {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying) {
                try {
                    mediaPlayer.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    interface OnMediaPlayerListener {
        fun mediaPlayed()
        fun mediaCompleted()
    }

    companion object {
        val instance = WMediaPlayer()
    }
}
