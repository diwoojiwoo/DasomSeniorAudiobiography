package com.onethefull.dasomautobiography

import android.app.Activity
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.manager.MentManager
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.repository.SplashRepository
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 6. 11.
 */
class SplashViewModel(
    private val context: Activity,
    private val repository: SplashRepository
) : BaseViewModel(), GCTextToSpeech.Callback {
    private val _isSpeechFinished = MutableLiveData(false)
    val isSpeechFinished: LiveData<Boolean> = _isSpeechFinished

    private val _contentLoaded = MutableLiveData(false)
    val contentLoaded: LiveData<Boolean> = _contentLoaded

    init {
        connect()
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
        GCTextToSpeech.getInstance()?.setCallback(this)
        GCTextToSpeech.getInstance()?.start(context)
    }

    fun disconnect() {
        DWLog.d("disconnect")
        GCTextToSpeech.getInstance()?.release()
    }

    fun getContent() {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if (check204) {
                repository.getContent(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
                    Build.SERIAL,
                ).let { response ->
                    when (response.statusCode) {
                        0 -> {
                            response.autobiography?.let { item ->
                                (context as MainActivity).viewModel.selectItem(
                                    Entry(
                                        autobiographyId = item.id,
                                        audioUrl = "",
                                        transText = "",
                                        imgUrl = item.imgUrl,
                                        question = item.question,
                                        answerYn = "",
                                        sort = "",
                                        type = item.type,
                                        typeName = item.typeName,
                                        viewQuestion = item.viewQuestion,
                                    )
                                )
                            } ?: run {
                                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                                RxBus.publish(RxEvent.destroyApp)
                            }
                            _contentLoaded.postValue(true)
                        }

                        else -> {
                            Toasty.error(context, context.getString(R.string.message_network_error)).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }
                    }
                }
            } else {
                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                RxBus.publish(RxEvent.destroyApp)
            }
        }
    }

    /*
    * 인트로, 시작, 멘트 가져오는용
    * */
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
                        1001 -> {
                            Toasty.error(context, context.getString(R.string.message_not_exist_elderly_info)).show()
                            RxBus.publish(RxEvent.destroyShortAppUpdate)
                        }

                        -3 -> {
                            Toasty.error(context, context.getString(R.string.message_not_registration_elderly)).show()
                            RxBus.publish(RxEvent.destroyShortAppUpdate)
                        }

                        0 -> {
                            when (MentManager.currentActionName) {
                                OnethefullBase.ACTION_SMARTFRIEND -> {
                                    response.smartfriendMent?.let { smartMent ->
                                        MentManager.smartfriendMent = smartMent
                                        val startText = smartMent.start ?: ""
                                        if (startText.isNotEmpty()) {
                                            GCTextToSpeech.getInstance()?.speech(startText)
                                        }
                                    }
                                }

                                OnethefullBase.ACTION_COMMAND -> {
                                    response.commandMent?.let { cmdMent ->
                                        MentManager.commandMent = cmdMent
                                        val startText = cmdMent.start ?: ""
                                        if (startText.isNotEmpty()) {
                                            GCTextToSpeech.getInstance()?.speech(startText)
                                        }
                                    }
                                }

                                else -> {
                                    Toasty.error(context, "Unexpected currentActionName: ${MentManager.currentActionName}").show()
                                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                                }
                            }
                        }

                        else -> {
                            Toasty.error(context, response.status).show()
                            RxBus.publish(RxEvent.destroyShortAppUpdate)
                        }
                    }
                }
            } else {
                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                RxBus.publish(RxEvent.destroyApp)
            }
        }
    }

    override fun onSpeechStart() {
        DWLog.d("onSpeechStart")
        _isSpeechFinished.value = false
    }

    override fun onSpeechFinish() {
        DWLog.d("onSpeechFinish")
        _isSpeechFinished.value = true
    }

    override fun onFinishService() {

    }

    override fun onWonderfulRequest(result: String) {

    }

    override fun onRequestHardwareControl(result: String) {

    }

    override fun onGenieSTTResult(result: String) {

    }

    override fun onGenieSTTResultNoAction(result: String) {

    }

    override fun onGenieCommandStart(result: String) {
    }

    override fun requestSendGenieLog(sttResult: String, genieResponse: String) {
    }
}