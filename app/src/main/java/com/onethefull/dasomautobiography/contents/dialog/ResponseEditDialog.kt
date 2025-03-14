package com.onethefull.dasomautobiography.contents.dialog

import android.content.Context
import android.content.DialogInterface
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

    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        binding.btnCheckAnswer.setOnClickListener {
            dialogListener?.checkAnswer()
        }

        binding.btnGoHome.setOnClickListener {
            dismiss()
            dialogListener?.moveHome()
        }

        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface?) {
        handler.removeCallbacksAndMessages(null)
        handler.sendMessageDelayed(Message(), 60_000)
    }

    override fun dismiss() {
        super.dismiss()
        handler.removeCallbacksAndMessages(null)
        dismissListener?.onDismiss()
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