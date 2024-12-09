package com.onethefull.dasomcontents.contents.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.onethefull.dasomcontents.App
import com.onethefull.dasomcontents.databinding.DialogHintBinding

/**
 * Created by sjw on 2024/12/25.
 */
class HintDialog(context: Context) : BaseDialog<DialogHintBinding>(context), DialogInterface.OnShowListener {
    var message: String? = ""
    var imgUrl: String? = ""
    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    fun setText(message: String): HintDialog {
        this.message = message
        return this
    }

    fun setImage(imgUrl: String): HintDialog {
        this.imgUrl = imgUrl
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
        if (message != "") {
            binding.tvHint.text = message
            binding.ivHint.visibility = View.GONE
        } else {
            Glide.with(context)
                .load(imgUrl)
                .centerCrop()
                .into(binding.ivHint)
            binding.tvHint.visibility = View.GONE
        }

        binding.btnConfirm.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            handler.sendEmptyMessage(0)
            dismiss()
        }
        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface?) {
        handler.removeCallbacksAndMessages(null)
        handler.sendMessageDelayed(Message(), 30 * 1000L)
    }
}