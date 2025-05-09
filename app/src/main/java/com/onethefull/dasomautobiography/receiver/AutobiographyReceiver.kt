package com.onethefull.dasomautobiography.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.data.api.ApiHelper
import com.onethefull.dasomautobiography.data.api.ApiHelperImpl
import com.onethefull.dasomautobiography.data.api.RetrofitBuilder
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by sjw on 2025. 3. 21.
 */
class AutobiographyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Constant.ACTION_STT_TEXT) {
            val logId = intent.getStringExtra(Constant.PARAM_LOG_ID) ?: return
            DWLog.d("AutobiographyReceiver - Received Action:${intent.action}, Log ID: $logId")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ApiHelperImpl(RetrofitBuilder.apiService).getLogDtl(
                        DasomProviderHelper.getCustomerCode(context),
                        DasomProviderHelper.getDeviceCode(context),
                        Build.SERIAL,
                        logId
                    ).let { response ->
                        when (response.statusCode) {
                            1001, -3 -> RxBus.publish(RxEvent.destroyApp)
                            0 -> {
                                response.autobiographyMap?.let {
                                    withContext(Dispatchers.Main) {
                                        val viewModel = ViewModelProvider(context as MainActivity)[MainViewModel::class.java]
                                        viewModel.setLogDtlApiResponse(it)
                                    }
                                }
                            }
                            else -> RxBus.publish(RxEvent.destroyApp)
                        }

                    }
                } catch (e: Exception) {
                    DWLog.e("API 호출 중 오류 발생 ${e.printStackTrace()}")
                    RxBus.publish(RxEvent.destroyApp)
                }
            }
        }
    }

}

