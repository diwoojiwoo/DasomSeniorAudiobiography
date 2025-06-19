package com.onethefull.dasomautobiography

import android.app.Activity
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
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
        // 응답없을경우 _isSpeechFinished.value = true 해서 앱 종료 시키기 추가
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
                                        typeName = item.type,
                                        viewQuestion = item.viewQuestion,
                                    )
                                )
                            } ?: run {
                                DWLog.d("????????????? 에러메세지 띄우고 앱종료")
                            }
                            GCTextToSpeech.getInstance()?.speech(response.introMent.toString())
                        }

                        else -> {
                            _isSpeechFinished.value = true
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