package com.onethefull.dasomcontents.ui.memoryquiz

import android.animation.ValueAnimator
import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onethefull.dasomcontents.App
import com.onethefull.dasomcontents.R
import com.onethefull.dasomcontents.contents.dialog.HintDialog
import com.onethefull.dasomcontents.contents.dialog.LoadingDialog
import com.onethefull.dasomcontents.contents.toast.Toasty
import com.onethefull.dasomcontents.data.model.memory.MemoryQuiz
import com.onethefull.dasomcontents.databinding.FragmentMemoryquizBinding
import com.onethefull.dasomcontents.utils.InjectorUtils
import com.onethefull.dasomcontents.utils.Status
import com.onethefull.dasomcontents.utils.bus.RxBus
import com.onethefull.dasomcontents.utils.bus.RxEvent
import com.onethefull.dasomcontents.utils.logger.DWLog
import com.onethefull.dasomcontents.utils.speech.SpeechStatus


// 추억소환문제 발화 시 진행 과정
// 추억소환퀴즈 시작->추억소환이미지발화->추억소환문제TTS발화->마이크ON->30초안에답변진행


class MemoryContentFragment : Fragment() {
    private lateinit var binding: FragmentMemoryquizBinding
    private var status: MemoryQuizStatus = MemoryQuizStatus.START

    val viewModel: MemoryContentViewModel by viewModels {
        InjectorUtils.provideMemoryContentViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        DWLog.d("onCreateView")
        binding = FragmentMemoryquizBinding.inflate(inflater, container, false).apply {
//            viewmodel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMemoryContents()
        setUpSpeech()

        binding.btnHint.setOnClickListener {
            activity?.let { mainActivity ->
                mainActivity.runOnUiThread {
                    if (viewModel.hint.value != null) {
                        HintDialog(mainActivity).setText(viewModel.hint.value.toString())
                            .apply { window?.requestFeature(Window.FEATURE_NO_TITLE) }
                            .run {
                                this.show()
                            }
                    } else {
                        HintDialog(mainActivity).setImage(viewModel.hintImgUrl.value.toString())
                            .apply { window?.requestFeature(Window.FEATURE_NO_TITLE) }
                            .run {
                                this.show()
                            }
                    }
                }
            }
        }

        binding.btnConfirm.setOnClickListener {
            RxBus.publish(RxEvent.destroyShortAppUpdate) // 5초 후 앱 종료

            binding.btnConfirm.visibility = View.GONE
            binding.ivAnswer.visibility = View.GONE

            binding.tvTitle.visibility = View.VISIBLE
            binding.tvQuestion.visibility = View.VISIBLE
            binding.tvAnswer.visibility = View.VISIBLE

            var params = binding.tvTitle.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = (64 * resources.displayMetrics.density).toInt()
            binding.tvTitle.layoutParams = params

            params = binding.tvQuestion.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = (136 * resources.displayMetrics.density).toInt()
            binding.tvQuestion.layoutParams = params
        }
    }

    private fun setUpSpeech() {
        viewModel.speechStatus.observe(viewLifecycleOwner) {
            changeStatus(it)
        }
    }

    private fun setUpMemoryContents() {
        DWLog.d("setUpMemoryContents")
        viewModel.checkQuizStatus(status)
        viewModel.question.observe(viewLifecycleOwner) {
            binding.tvQuestion.text = it.toString()
            binding.btnHint.visibility = View.VISIBLE
        }

        viewModel.answer.observe(viewLifecycleOwner) {
            binding.tvAnswer.text = it.toString()
        }

        viewModel.imgUrl.observe(viewLifecycleOwner) {
            Glide.with(this)
                .load(it)
//                .placeholder(R.drawable.uber)
//                .error(R.drawable.uber)
                .centerCrop()
                .into(binding.ivBg!!)
        }

        viewModel.toAnswer.observe(viewLifecycleOwner) {
            binding.tvTitle.visibility = View.GONE
            binding.btnHint.visibility = View.GONE
            binding.tvQuestion.visibility = View.GONE
            binding.tvAnswer.visibility = View.GONE
            binding.aniMic.visibility = View.GONE

            if (it) {
                binding.ivAnswer.setBackgroundResource(R.drawable.correct)
            } else {
                binding.ivAnswer.setBackgroundResource(R.drawable.wrong)
                Handler().postDelayed({
                    var params = binding.ivAnswer.layoutParams as ConstraintLayout.LayoutParams
                    params.width = (348 * resources.displayMetrics.density).toInt()
                    params.height = (348 * resources.displayMetrics.density).toInt()
                    params.topMargin = (48 * resources.displayMetrics.density).toInt()
                    binding.ivAnswer.layoutParams = params

                    // "정답 확인하기" 버튼 보이기
                    binding.btnConfirm.visibility = View.VISIBLE
                }, 5 * 1000L)
            }
        }

        viewModel.inputText.observe(viewLifecycleOwner) {
            MediaPlayer.create(App.instance.currentActivity, R.raw.d54).apply {
                setOnPreparedListener {
                    Handler(Looper.getMainLooper()).postDelayed({
                        it.start()
                    }, 100)
                }
                setOnCompletionListener { it.release() }
            }
            showToast(it)
        }

        viewModel.quizComments().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { result ->
//                        val type = object : TypeToken<MemoryQuiz>() {}.type
//                        val quiz: MemoryQuiz = Gson().fromJson(result, type)
                    }
                }

                Status.ERROR -> {
                    it.data?.let { result ->
                        showToast(result)
                        RxBus.publish(RxEvent.destroyShortAppUpdate)
                    }
                }

                Status.LOADING -> {

                }
            }
        }
    }

    private fun changeStatus(status: SpeechStatus) {
        DWLog.i("changeStatus animation == [$status]")
        when (status) {
            SpeechStatus.WAITING -> {
                binding.aniMic.visibility = View.VISIBLE
            }

            SpeechStatus.SPEECH -> {
                binding.aniMic.visibility = View.GONE
            }
        }
    }


    private fun showToast(message: String) {
        activity?.let {
            it.runOnUiThread {
                Toasty.success(it, message, Toast.LENGTH_SHORT, true).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }


    companion object {
    }
}