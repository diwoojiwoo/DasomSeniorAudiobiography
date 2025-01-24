package com.onethefull.dasomautobiography.contents.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.databinding.DialogNotExistBinding

/**
 * Created by sjw on 2024/12/2.
 */
class NotExistDialog(context: Context) : BaseDialog<DialogNotExistBinding>(context), DialogInterface.OnShowListener {
    var message: String? = ""
    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    fun setText(message: String): NotExistDialog {
        this.message = message
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setBackgroundDrawableResource(R.color.transparent)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface?) {
        handler.removeCallbacksAndMessages(null)
        handler.sendMessageDelayed(Message(), 20 * 1000L)
    }
}