package com.onethefull.dasomautobiography.contents.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.view.WindowManager
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.databinding.DialogPopupBinding
import com.onethefull.dasomautobiography.databinding.DialogResultBinding

/**
 * Created by sjw on 2024/12/25.
 */
class PopupDialog(context: Context) : BaseDialog<DialogPopupBinding>(context), DialogInterface.OnShowListener {
    var title: String? = ""
    var message: String? = ""
    private var dialogListener: DialogListener? = null

    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    fun setText(title : String, message : String) : PopupDialog {
        this.title = title
        this.message = message
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(0x80000000.toInt()))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
        }

        binding.tvTitle.text = title
        binding.tvTitleDetail.text = message

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            dismiss()
            dialogListener?.delete()
        }

        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface?) {
        handler.removeCallbacksAndMessages(null)
        handler.sendMessageDelayed(Message(), 60__000)
    }

    fun setDialogListener(dialogListener: DialogListener?) {
        this.dialogListener = dialogListener
    }

    interface DialogListener {
        fun delete()
    }
}