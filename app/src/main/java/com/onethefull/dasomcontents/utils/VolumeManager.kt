package com.onethefull.dasomcontents.utils

import android.content.Context
import android.media.AudioManager
import java.util.*
import kotlin.math.abs

/**
 * Created by sjw on 13,October,2020
 */
object VolumeManager {
    private val VOLUMES: ArrayList<Int> = object : ArrayList<Int>() {
        init {
            this.add(0, 0)
            this.add(1, 2)
            this.add(2, 4)
            this.add(3, 6)
            this.add(4, 8)
            this.add(5, 10)
        }
    }
    private const val LEVEL_MIN = 1
    private var LEVELS = 0
    private var LEVEL_MAX = 0
    private var MAX_VOLUME = 0

    operator fun get(context: Context): Int {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = manager.getStreamVolume(3)
        return getLevel(volume)
    }

    fun getMin(): Int {
        return 1
    }

    fun getMax(): Int {
        return LEVEL_MAX
    }

    fun getPercent(context: Context): Int {
        val manager = context.getSystemService("audio") as AudioManager
        val volume = manager.getStreamVolume(3)
        return getPercent(
            getLevel(volume)
        )
    }

    fun getPercent(level: Int): Int {
        return level * 100 / LEVEL_MAX
    }

    fun up(context: Context): Int {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = manager.getStreamVolume(3)
        var position =
            getLevel(volume)
        if (position < VOLUMES.size - 1) {
            ++position
        } else {
            position = VOLUMES.size - 1
        }
        setVolume(
            manager,
            VOLUMES[position]
        )
        return position
    }

    fun low(context: Context): Int {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = manager.getStreamVolume(3)
        var position =
            getLevel(volume)
        if (position > 0) {
            --position
        } else {
            position = 0
        }
        setVolume(
            manager,
            VOLUMES[position]
        )
        return position
    }

    fun max(context: Context): Int {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setVolume(
            manager,
            VOLUMES[LEVEL_MAX]
        )
        return LEVEL_MAX
    }

    fun min(context: Context): Int {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setVolume(
            manager,
            VOLUMES[1]
        )
        return 1
    }

    fun mute(context: Context): Int {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setVolume(manager, 0)
        return 0
    }

    operator fun set(context: Context, percent: Int): Int {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val position =
            getLevel(Math.round((percent * MAX_VOLUME / 100).toFloat()))
        setVolume(
            manager,
            VOLUMES[position]
        )
        return position
    }

    fun setLevel(context: Context, level: Int): Int {
        var level = level
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (level > LEVEL_MAX) {
            level =
                getLevel(level)
        }
        setVolume(
            manager,
            VOLUMES[level]
        )
        return level
    }

    private fun getLevel(volume: Int): Int {
        var position = 0
        for (i in 0 until VOLUMES.size - 1) {
            val pVolume = VOLUMES[i]
            val pVolumeNext = VOLUMES[i + 1]
            if (volume in pVolume..pVolumeNext) {
                val left = abs(volume - pVolume)
                val right = abs(volume - pVolumeNext)
                position = if (left >= right) i + 1 else i
                break
            }
        }
        return position
    }

    private fun setVolume(manager: AudioManager, volume: Int) {
        var volume = volume
        if (volume > MAX_VOLUME) {
            volume = MAX_VOLUME
        }
        if (volume < 0) {
            volume = 0
        }
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 4)
    }

    init {
        LEVELS = VOLUMES.size
        LEVEL_MAX = LEVELS - 1
        MAX_VOLUME = VOLUMES[LEVEL_MAX]
    }

}
