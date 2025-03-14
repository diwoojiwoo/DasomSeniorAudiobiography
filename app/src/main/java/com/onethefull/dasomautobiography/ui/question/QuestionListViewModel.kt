package com.onethefull.dasomautobiography.ui.question

import android.app.Activity
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.base.BaseViewModel
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.audiobiography.Item
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.repository.MenuRepository
import com.onethefull.dasomautobiography.repository.QuestionListRepository
import com.onethefull.dasomautobiography.utils.WMediaPlayer
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 1. 9.
 */

class QuestionListViewModel(
    private val context: Activity,
    private val repository: QuestionListRepository
) : BaseViewModel() {
    // RecyclerView의 데이터 리스트를 저장할 LiveData
    private val _itemList = MutableLiveData<List<Item>>()
    val itemList: LiveData<List<Item>> get() = _itemList

    init {
        connect()
    }

    private fun connect() {
        Thread.sleep(500L)
        DWLog.d("connect")
    }

    fun disconnect() {
        DWLog.d("disconnect")
    }

    fun requestQuestionList(type: String) {
        uiScope.launch {
            val check204 = repository.check204() ?: false
            if(check204) {
                repository.getQuestionList(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context),
                    Build.SERIAL,
                    type
                ).let { response ->
                    when(response.statusCode) {
                        1001 -> {
                            Toasty.error(context, context.getString(R.string.message_not_exist_elderly_info)).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }

                        -3 -> {
                            Toasty.error(context, context.getString(R.string.message_not_registration_elderly)).show()
                            RxBus.publish(RxEvent.destroyApp)
                        }
                        0 -> {
                            _itemList.value = response.list
                        }
                    }

                }
            } else {
                Toasty.error(context, context.getString(R.string.message_network_error)).show()
                RxBus.publish(RxEvent.destroyApp)
            }
        }
    }
}