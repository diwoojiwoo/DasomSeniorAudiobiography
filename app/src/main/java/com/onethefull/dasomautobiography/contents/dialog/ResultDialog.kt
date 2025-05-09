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
import com.onethefull.dasomautobiography.databinding.DialogResultBinding
import com.onethefull.dasomautobiography.utils.logger.DWLog

/**
 * Created by sjw on 2024/12/25.
 */
class ResultDialog(context: Context) : BaseDialog<DialogResultBinding>(context), DialogInterface.OnShowListener {
    var title: String? = ""
    var message1: String? = ""
    var message2: String? = ""
    private var dialogListener: DialogListener? = null
    private var dismissListener: DialogDismissListener? = null

    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    fun setText(title: String, message1: String, message2 : String): ResultDialog {
        this.title = title
        this.message1 = message1
        this.message2 = message2
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        binding.tvTitle.text = title
        binding.btnCheckAnswer.text = message1
        binding.btnGoHome.text = message2

        binding.btnCheckAnswer.setOnClickListener {
            dismiss()
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
        handler.sendMessageDelayed(Message(), 20_000)
    }

    override fun dismiss() {
        super.dismiss()
        DWLog.e("dismiss")
        handler.removeCallbacksAndMessages(null)
        dismissListener?.onDismiss()
    }

    fun setDialogListener(dialogListener: DialogListener?) {
        this.dialogListener = dialogListener
    }

    fun setDismissListener(dismissListener: DialogDismissListener?): ResultDialog {
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