package com.onethefull.dasomcontents

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.multidex.MultiDexApplication
import com.onethefull.dasomcontents.utils.settings.BaseSettings
import com.onethefull.dasomcontents.utils.VolumeManager
import com.onethefull.dasomcontents.utils.logger.DWLog
import com.onethefull.wonderfulrobotmodule.scene.SceneHelper
import com.onethefull.wonderfulrobotmodule.scene.SceneEventListener
import kotlinx.coroutines.Job
import java.io.Serializable
import java.util.*

/**
 * Created by sjw on 2021/11/10
 */

class App : MultiDexApplication() {
    var currentActivity: Activity? = null
    private var mWakeLock: PowerManager.WakeLock? = null
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
        SceneHelper.setSceneEventListener(object :SceneEventListener() {
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
                mWakeLock =
                    (instance.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                        .newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK or
                                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                                    PowerManager.ON_AFTER_RELEASE,
                            "Tag:DasomContentsPower"
                        )
                mWakeLock?.acquire()
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
        send.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        DWLog.w("onCommand action:$action")

        when (action) {
            "Open"-> {

            } else ->{

            }
        }

        startActionActivity(send)
    }


    fun getLocale(): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            instance?.resources?.configuration?.locales?.get(0)
        } else {
            instance?.resources?.configuration?.locale
        }
    }

    private fun startActionActivity(send: Intent) {
        if (currentActivity != null && currentActivity is MainActivity) {
            (currentActivity as MainActivity).apply {
                intent = send
            }.run {
                startFragment()
            }
        } else {
            send.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(send)
        }
    }

    val jobList = HashMap<String, Job>()

    fun releaseJob() {
        jobList.forEach {
            it.value.cancel()
        }
    }

    var isRunning = false

    companion object {
        lateinit var instance: App
            private set

        const val TAG = "[DasomSeniorContents]"
    }
}