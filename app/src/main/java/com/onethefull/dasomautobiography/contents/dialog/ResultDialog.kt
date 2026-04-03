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
import com.onethefull.dasomautobiography.databinding.DialogResultBinding
import com.onethefull.dasomautobiography.utils.logger.DWLog
import androidx.core.graphics.drawable.toDrawable

/**
 * Created by sjw on 2024/12/25.
 */
class ResultDialog(context: Context) : BaseDialog<DialogResultBinding>(context), DialogInterface.OnShowListener {
    var title: String? = ""
    var message1: String? = ""
    var message2: String? = ""
    private var dialogListener: DialogListener? = null

    var onAutoDismiss: (() -> Unit)? = null

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
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
        handler.postDelayed({
            dismiss()
            onAutoDismiss?.invoke()
        }, 20_000) // 20초 후 자동 dismiss
    }

    override fun dismiss() {
        super.dismiss()
        DWLog.e("dismiss")
        handler.removeCallbacksAndMessages(null)
    }

    fun setDialogListener(dialogListener: DialogListener?) {
        this.dialogListener = dialogListener
    }

    interface DialogListener {
        fun checkAnswer()
        fun moveHome()
    }
}