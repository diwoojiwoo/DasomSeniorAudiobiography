package com.onethefull.dasomautobiography.contents.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.databinding.DialogAnswerBinding

/**
 * Created by sjw on 2024/12/25.
 */
class AnswerDialog(context: Context) : BaseDialog<DialogAnswerBinding>(context), DialogInterface.OnShowListener {
    var message: String? = ""
    val semiTransparentBlue = Color.argb(76, 0, 0, 255) // 128은 알파 값 (0~255)
    val semiTransparentRed = Color.argb(76, 255, 0, 0) // 50% 투명 빨간색
    private var dialogListener: DialogListener? = null

    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    fun setText(message: String): AnswerDialog {
        this.message = message
        return this
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            when (message) {
                "Y" -> {
                    setBackgroundDrawable(ColorDrawable(semiTransparentBlue))
                    binding.ivAnswer.setBackgroundResource(R.drawable.correct)
                    binding.btnConfirm.visibility = View.GONE
                    binding.btnNextQuiz.visibility = View.VISIBLE

                    var params = binding.ivAnswer.layoutParams as FrameLayout.LayoutParams
                    params.width = (400 * context.resources.displayMetrics.density).toInt()
                    params.height = (400 * context.resources.displayMetrics.density).toInt()
                    params.topMargin = 24
                    params.gravity  = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    binding.ivAnswer.layoutParams = params

                }
                "N" -> {
                    setBackgroundDrawable(ColorDrawable(semiTransparentRed))
                    binding.ivAnswer.setBackgroundResource(R.drawable.wrong)
                    binding.btnConfirm.visibility = View.GONE
                    binding.btnNextQuiz.visibility = View.GONE

                    var params = binding.ivAnswer.layoutParams as FrameLayout.LayoutParams
                    params.width = (480 * context.resources.displayMetrics.density).toInt()
                    params.height = (480 * context.resources.displayMetrics.density).toInt()
                    params.topMargin = 0
                    params.gravity  = Gravity.CENTER
                    binding.ivAnswer.layoutParams = params

                }
                "N2" -> {
                    setBackgroundDrawable(ColorDrawable(semiTransparentRed))
                    binding.ivAnswer.setBackgroundResource(R.drawable.wrong)

                    var params = binding.ivAnswer.layoutParams as FrameLayout.LayoutParams
                    params.width = (308 * context.resources.displayMetrics.density).toInt()
                    params.height = (308 * context.resources.displayMetrics.density).toInt()
                    params.gravity  = Gravity.CENTER
                    binding.ivAnswer.layoutParams = params
                    binding.btnConfirm.visibility = View.VISIBLE
                    binding.btnNextQuiz.visibility = View.GONE
                }
            }
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        binding.btnConfirm.setOnClickListener {
            dismiss()
            dialogListener?.checkAnswer()
        }

        binding.btnNextQuiz.setOnClickListener {
            dismiss()
            dialogListener?.nextQuiz()
        }

        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface?) {
        handler.removeCallbacksAndMessages(null)

        when(message) {
            "N"-> {
                handler.sendMessageDelayed(Message(), 3 * 1000L)
            }
            "Y", "N2"-> {
                handler.sendMessageDelayed(Message(), 60 * 1000L)
            }
        }
    }

    fun setDialogListener(dialogListener: DialogListener?) {
        this.dialogListener = dialogListener
    }

    interface DialogListener {
        fun checkAnswer()
        fun nextQuiz()
    }
}