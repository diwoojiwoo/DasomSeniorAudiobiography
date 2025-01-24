package com.onethefull.dasomautobiography

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.multidex.MultiDexApplication
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.utils.settings.BaseSettings
import com.onethefull.dasomautobiography.utils.VolumeManager
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.wonderfulrobotmodule.scene.SceneHelper
import com.onethefull.wonderfulrobotmodule.scene.SceneEventListener
import java.io.Serializable
import java.util.*

/**
 * Created by sjw on 2021/11/10
 */

class App : MultiDexApplication() {
    var currentActivity: Activity? = null
    var defaultLanguage: Locale? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        initSceneHelper()
        updateLocale()
    }

    fun updateLocale() {
        defaultLanguage = BaseSettings.getSystemLocale(this)
    }

    /**
     * init SceneHelper and add SceneEvent Listener
     */
    private fun initSceneHelper() {
        DWLog.e("init")
        SceneHelper.initialize(this)
        SceneHelper.setSceneEventListener(object : SceneEventListener() {
            override fun onSwitchIn(flags: Int) {
                super.onSwitchIn(flags)
                DWLog.d("onSwitchIn")
            }

            override fun onSwitchOut() {
                DWLog.d("onSwitchOut")
                super.onSwitchOut()
                mWakeLock?.release()
            }

            override fun onCommand(
                action: String?,
                params: Bundle?,
                suggestion: Serializable?,
            ) {
                super.onCommand(action, params, suggestion)
                wakeLock()
                this@App.onCommand(action, params, suggestion)
                return
            }
        })
    }

    /**
     * Scene onCommand 공통 동작
     */
    fun onCommand(action: String?, params: Bundle?, suggestion: Serializable?) {
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
            if (VolumeManager[this@App] == 1) {
                VolumeManager.setLevel(this@App, 1)
            } else if (VolumeManager[this@App] == 2) {
                VolumeManager.setLevel(this@App, 2)
            }
        }
        val send = Intent(instance, MainActivity::class.java)
        send.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        send.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        DWLog.w("onCommand action:$action")

        when (action) {
            OnethefullBase.ACTION_OPEN -> {
                send.putExtra(
                    OnethefullBase.PARAM_ACTION_NAME,
                    action
                )
            }
            else -> {

            }
        }
        params?.let {
            send.putExtras(it)
        }
        startActivity(send, ActivityOptions.makeCustomAnimation(this@App, 0, 0).toBundle())
    }


    fun getLocale(): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            instance?.resources?.configuration?.locales?.get(0)
        } else {
            instance?.resources?.configuration?.locale
        }
    }

    var isRunning = false

    private fun wakeLock() {
        mWakeLock =
            (instance.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or
                            PowerManager.ACQUIRE_CAUSES_WAKEUP or
                            PowerManager.ON_AFTER_RELEASE,
                    "Tag:SoodaPower"
                )
        mWakeLock?.acquire()
    }

    private var mWakeLock: PowerManager.WakeLock? = null

    companion object {
        lateinit var instance: App
            private set

        const val TAG = "[DasomSeniorAudiobiography]"
    }
}