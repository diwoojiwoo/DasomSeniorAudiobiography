package com.onethefull.dasomautobiography.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.data.api.ApiHelper
import com.onethefull.dasomautobiography.data.api.ApiHelperImpl
import com.onethefull.dasomautobiography.data.api.RetrofitBuilder
import com.onethefull.dasomautobiography.provider.DasomProviderHelper
import com.onethefull.dasomautobiography.ui.questiondetail.QuestionDetailFragment
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

            if (context == null) return

            // 1. 현재 화면에 QuestionDetailFragment가 붙어 있는지 체크
            val activity = context as? FragmentActivity ?: return
            val fragment = activity.supportFragmentManager.findFragmentByTag("QuestionDetailFragmentTag")

            if (fragment is QuestionDetailFragment && fragment.isVisible) {
                // 2. QuestionDetailFragment가 화면에 보이고 있으면 API 호출
                fragment.viewModel.getLogDtl(logId)
            } else {

            }
        }
    }

}

