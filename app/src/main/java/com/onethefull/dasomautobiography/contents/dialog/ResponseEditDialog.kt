package com.onethefull.dasomautobiography.contents.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.view.WindowManager
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.databinding.DialogResponseEditBinding
import com.onethefull.dasomautobiography.databinding.DialogResultBinding
import com.onethefull.dasomautobiography.utils.logger.DWLog

/**
 * Created by sjw on 2024/12/25.
 */
class ResponseEditDialog(context: Context) : BaseDialog<DialogResponseEditBinding>(context), DialogInterface.OnShowListener {
    private var dialogListener: DialogListener? = null
    private var dismissListener: DialogDismissListener? = null

    private var isAutoDismiss = false

    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) {
            isAutoDismiss = true
            dismiss()
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.btnCheckAnswer.setOnClickListener {
            dialogListener?.checkAnswer()
            dismissListener?.let { /* 호출하지 않음 */ }
            dismissWithoutListener()
        }

        binding.btnGoHome.setOnClickListener {
            dialogListener?.moveHome()
            dismiss() // 이때는 dismissListener 호출 가능
        }

        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface?) {
        isAutoDismiss = true
        handler.removeCallbacksAndMessages(null)
        handler.sendMessageDelayed(Message(), 20_000)
    }

    override fun dismiss() {
        super.dismiss()
        handler.removeCallbacksAndMessages(null)
        if (isAutoDismiss) {
            dismissListener?.onDismiss()
        }
    }

    private fun dismissWithoutListener() {
        isAutoDismiss = false
        super.dismiss()
        handler.removeCallbacksAndMessages(null)
    }

    fun setDialogListener(dialogListener: DialogListener?) {
        this.dialogListener = dialogListener
    }

    fun setDismissListener(dismissListener: DialogDismissListener?): ResponseEditDialog {
        this.dismissListener = dismissListener
        return this
    }

    interface DialogListener {
        fun checkAnswer()
        fun moveHome()
    }

    interface DialogDismissListener {
        fun onDismiss()
    }
}