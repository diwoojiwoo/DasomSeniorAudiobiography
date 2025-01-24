package com.onethefull.dasomautobiography

import android.os.Handler
import android.os.Looper
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.wonderfulrobotmodule.scene.SceneHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


/**
 * Created by sjw on 2021/11/23
 */
class MainViewModel : BaseViewModel() {
    /**
     * Excute Event Bus
     */
    private val BusExcute: (event: RxEvent.Event) -> Unit = { event ->

        DWLog.d("BusExcute $event")

        when (event.typeNumber) {
            RxEvent.AppDestroy -> {
                handler.removeMessages(MESSAGE_WHAT_TERMINATE_APP)
                SceneHelper.switchOut()
                App.instance.currentActivity?.finish()
            }

            RxEvent.AppDestroyUpdate -> {
                updateTerminator(event.time)
            }

            RxEvent.AppDestroyRemove -> {
                removeTerminator()
            }

            RxEvent.SpeechDelayUpdate -> {
                updateDelaySpeech(event.time)
            }
        }
    }

    fun start() {
        DWLog.d("Start")
        eventRegister()
        updateTerminator(TIME_TERMINATE_APP) // 앱종료 체커 시작
    }

    fun release() {
        compositeDisposable.dispose()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Register Event
     */
    private val compositeDisposable = CompositeDisposable()

    /**
     *
     */
    private val eventRegister: () -> Unit = {
        compositeDisposable.add(
            RxBus.listen(RxEvent.Event::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(BusExcute)
        )
    }


    private val mHandlerCallback = Handler.Callback { msg ->
        when (msg.what) {
            MESSAGE_WHAT_TERMINATE_APP -> {
                SceneHelper.switchOut()
                App.instance.currentActivity?.finishAffinity()
            }
            MESSAGE_WHAT_DELAY_SPEECH -> {

            }
        }
        false
    }

    private val handler = Handler(Looper.getMainLooper(), mHandlerCallback)

    /**
     * 자동 종료 시간 갱신
     */
    private fun updateTerminator(time: Long) {
        DWLog.i("MESSAGE_WHAT_TERMINATE_APP ==> updateTerminator $time")
        removeTerminator()
        handler.sendMessageDelayed(
            handler.obtainMessage(MESSAGE_WHAT_TERMINATE_APP),
            time
        )
    }


    /**
     * 자동 종료 제거
     */
    private fun removeTerminator() {
        DWLog.i("MESSAGE_WHAT_TERMINATE_APP ==> removeTerminator")
        handler.removeMessages(MESSAGE_WHAT_TERMINATE_APP)
    }

    /**
     * 자동 종료 발화 시간 갱신
     */
    private fun updateDelaySpeech(time: Long) {
        DWLog.i("MESSAGE_WHAT_DELAY_SPEECH")
        removeDelaySpeech()
        handler.sendMessageDelayed(
            handler.obtainMessage(MESSAGE_WHAT_DELAY_SPEECH),
            time
        )
    }

    /**
     * 자동 종료 발화 제거
     */
    private fun removeDelaySpeech() {
        DWLog.i("MESSAGE_WHAT_DELAY_SPEECH ==> removeDelaySpeech")
        handler.removeMessages(MESSAGE_WHAT_DELAY_SPEECH)
    }

    override fun onCleared() {
        super.onCleared()
    }

    companion object {
        // TUTRIAL TIME
        const val TIME_TERMINATE_APP = 90 * 1000L

        // MESSAGE ID
        const val MESSAGE_WHAT_TERMINATE_APP = 0x202
        const val MESSAGE_WHAT_DELAY_SPEECH = 0x203
    }

}