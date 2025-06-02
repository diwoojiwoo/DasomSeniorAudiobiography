package com.onethefull.dasomautobiography

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.data.model.audiobiography.AutobiographyMap
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
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

            RxEvent.NavigateToMenuFragment-> {
                updateNavigator(event.time)
            }

            RxEvent.RemoveNavigateToMenuFragment-> {
                removeNavigator()
            }
        }
    }

    private val mHandlerCallback = Handler.Callback { msg ->
        when (msg.what) {
            MESSAGE_WHAT_TERMINATE_APP -> {
                SceneHelper.switchOut()
                App.instance.currentActivity?.finishAffinity()
            }

            MESSAGE_WHAT_NAVIGATE_MENU_FRAGMENT-> {
                DWLog.d("NavigateToMenuFragment")
                App.instance.currentActivity?.findNavController(R.id.nav_host)?.navigate(R.id.action_speech_to_menu_fragment)
            }
        }
        false
    }

    private val handler = Handler(Looper.getMainLooper(), mHandlerCallback)

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

    private fun updateNavigator(time: Long) {
        DWLog.i("MESSAGE_WHAT_NAVIGATE_MENU_FRAGMENT ==> updateNavigator $time")
        removeNavigator()
        handler.sendMessageDelayed(
            handler.obtainMessage(MESSAGE_WHAT_NAVIGATE_MENU_FRAGMENT),
            time
        )
    }


    /**
     * 자동 SpeechFragment에서 MenuFragment 화면으로 이동 제거
     */
    private fun removeNavigator() {
        DWLog.i("MESSAGE_WHAT_NAVIGATE_MENU_FRAGMENT ==> removeNavigator")
        handler.removeMessages(MESSAGE_WHAT_NAVIGATE_MENU_FRAGMENT)
    }

    override fun onCleared() {
        super.onCleared()
    }

    /**
     * fragment 간 값 공유
     */
    private val _selectedItem = MutableLiveData<Entry>()
    val selectedItem: LiveData<Entry> get() = _selectedItem

    fun selectItem(entry: Entry) {
        _selectedItem.value = entry
    }


    /**
     * 로그 아이디를 통해 자서전 로그 상세 값 저장
     */
    private val _logDtlApiResponse = MutableLiveData<AutobiographyMap>()
    val logDtlApiResponse : LiveData<AutobiographyMap> get() = _logDtlApiResponse

    fun setLogDtlApiResponse(map: AutobiographyMap) {
        _logDtlApiResponse.value = map
    }

    companion object {
        const val TIME_TERMINATE_APP = 90 * 1000L

        // MESSAGE ID
        const val MESSAGE_WHAT_TERMINATE_APP = 0x202
        const val MESSAGE_WHAT_NAVIGATE_MENU_FRAGMENT = 0x204
    }

}