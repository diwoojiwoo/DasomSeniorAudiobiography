package com.onethefull.dasomautobiography.ui.menu

import android.app.Activity
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.Status
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.repository.MenuRepository
import com.onethefull.dasomautobiography.utils.WMediaPlayer
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.network.NetworkStatusCode
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 1. 9.
 */

class MenuViewModel(
    private val context: Activity,
    private val repository: MenuRepository
) : BaseViewModel(), GCTextToSpeech.Callback {
    private val _items = MutableLiveData<List<MenuItem2>>()
    val items: LiveData<List<MenuItem2>> get() = _items

    init {
        connect()
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
        GCTextToSpeech.getInstance()?.setCallback(this)
        GCTextToSpeech.getInstance()?.start(context)
        RxBus.publish(RxEvent.Event(RxEvent.AppDestroyUpdate, 60 * 1000L, "AppDestroyUpdate"))
    }

    fun disconnect() {
        GCTextToSpeech.getInstance()?.release()
        WMediaPlayer.instance.setListener(null)
    }

    /**
     * 자서전 메뉴 가져오기
     * */
    private val _categoryStatusEvent = MutableLiveData<Status>()
    val categoryStatusEvent: LiveData<Status> get() = _categoryStatusEvent
    fun getCategoryList() {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if (check204) {
                repository.getCategoryListV2(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
                    Build.SERIAL,
                ).let { response ->
                    when (response.statusCode) {
                        NetworkStatusCode.ERROR_ELDERLY_INFO_NOT_EXIST-> {
                            Toasty.error(context, context.getString(R.string.message_not_exist_elderly_info)).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }

                        NetworkStatusCode.ERROR_ELDERLY_NOT_REGISTERED-> {
                            Toasty.error(context, context.getString(R.string.message_not_registration_elderly)).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }

                        NetworkStatusCode.SUCCESS -> {
                            _items.value = response.cateList ?: emptyList()
                            _categoryStatusEvent.postValue(Status(0, "success"))
                        }

                        else -> {
                            _categoryStatusEvent.postValue(Status(response.statusCode, response.status ?: "알 수 없는 오류"))
                        }
                    }
                }
            } else {
                _categoryStatusEvent.postValue(Status(NetworkStatusCode.ERROR_NETWORK, context.getString(R.string.message_network_error)))
            }
        }
    }

    /***
     * GCTextToSpeech
     * */
    // TTS 출력 시작
    override fun onSpeechStart() {
        DWLog.d("onSpeechStart")
        speechStarted()
    }

    // TTS 출력 종료
    override fun onSpeechFinish() {
        DWLog.d("onSpeechFinish")
        speechFinished()
    }

    // 음성출력 시작
    private fun speechStarted() {
    }

    // 음성출력 종료
    private fun speechFinished() {
        changeStatusSpeechFinished()
        checkCurrentStatus()
    }

    /**
     * TTS 출력이 끝난 상태 변경
     */
    private fun changeStatusSpeechFinished() {}

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {}

    override fun onGenieSTTResult(result: String) {}

    override fun onFinishService() {
        TODO("Not yet implemented")
    }

    override fun onWonderfulRequest(result: String) {
        TODO("Not yet implemented")
    }

    override fun onRequestHardwareControl(result: String) {
        TODO("Not yet implemented")
    }

    override fun onGenieSTTResultNoAction(result: String) {
        TODO("Not yet implemented")
    }

    override fun onGenieCommandStart(result: String) {
        TODO("Not yet implemented")
    }

    override fun requestSendGenieLog(sttResult: String, genieResponse: String) {
        TODO("Not yet implemented")
    }
}