package com.onethefull.dasomautobiography.contents.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.onethefull.dasomautobiography.App
import androidx.core.graphics.drawable.toDrawable
import com.onethefull.dasomautobiography.databinding.DialogFulltextBinding

/**
 * Created by sjw on 2024/12/25.
 */
class FullTextDialog(context: Context) : BaseDialog<DialogFulltextBinding>(context), DialogInterface.OnShowListener {
    private var dialogListener: DialogListener? = null
    var ttsText: String? = null
    var ttsCallback: ((String) -> Unit)? = null
    var tvSpeechStatus: String? = null
    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    fun setTtsText(text: String, callback: (String) -> Unit, speechStatus: String): FullTextDialog {
        ttsText = text
        ttsCallback = callback
        tvSpeechStatus = speechStatus
        return this
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(0x80000000.toInt().toDrawable())
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
        }

        if (!ttsText.isNullOrEmpty()) {
            binding.tvContent.text = ttsText
        }

        binding.tvSpeech.text = tvSpeechStatus

        binding.ivCancel.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            handler.sendEmptyMessage(0)

            dialogListener?.onCancelTts()

            dismiss()
        }

        setOnShowListener(this) // 다이얼로그가 보일 때 콜백
    }

    fun setDialogListener(dialogListener: DialogListener?): FullTextDialog {
        this.dialogListener = dialogListener
        return this
    }

    interface DialogListener {
        fun onCancelTts()
    }

    override fun onShow(dialog: DialogInterface?) {
        ttsText?.let { text ->
            ttsCallback?.invoke(text)
        }

        handler.removeCallbacksAndMessages(null)
        handler.sendMessageDelayed(Message(), 2 * 60 * 1000L)

    }
}