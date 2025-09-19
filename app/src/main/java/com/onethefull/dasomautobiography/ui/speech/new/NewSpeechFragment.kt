package com.onethefull.dasomautobiography.ui.speech.new

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.contents.dialog.ResultDialog
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus
import androidx.navigation.NavOptions
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.databinding.FragmentNewSpeechBinding
import com.onethefull.dasomautobiography.utils.dpToPx
import com.onethefull.dasomautobiography.utils.network.NetworkStatusCode
import com.onethefull.wonderfulrobotmodule.ext.dasomLanguageCodeValue


class NewSpeechFragment : Fragment() {
    private lateinit var binding: FragmentNewSpeechBinding
    val viewModel: NewSpeechViewModel by viewModels {
        InjectorUtils.provideNewSpeechViewModelFactory(requireContext())
    }
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val limitTimeMillis: Long = 60_000L
    private var startTimeMillis: Long = 0L
    private val offset = 1L
    private var timeWhenPlayStopped: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        binding.includeNewRecord.apply {
            layoutMediaPlay.isEnabled = false
            layoutSend.isEnabled = false
            chRecState.isChecked = false
            chRecState.isEnabled = true
            tvRecState.text = resources.getString(R.string.recording_start)
            tvRecState.setTextColor(Color.WHITE)

            val alpha = chRecState.background
            alpha.alpha = 255
        }
        binding.chronometer.base = SystemClock.elapsedRealtime()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewSpeechBinding.inflate(inflater, container, false).apply {}
        viewModel.fetchDataFromSharedViewModel(sharedViewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상황인식 변수 확인
        val motionDetected = arguments?.getBoolean(Constant.PARAM_MOTION_DETECTED) ?: false
        DWLog.d("SpeechFragment - motionDetected = $motionDetected")
        viewModel.setMotionDetected(motionDetected)

        val language = App.instance.getLocale()?.dasomLanguageCodeValue() ?: Constant.KO
        when (language) {
            "en-US" -> {
                binding.tvQuestionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
                binding.includeNewRecord.tvRecState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24.5f)
            }

            "ja-JP" -> {
                binding.tvTitleSpeech.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
                binding.tvQuestionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                binding.includeNewRecord.tvRecState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            }

            else -> { // 한국, 번체, 간체
                binding.tvTitleSpeech.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
                binding.tvQuestionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            }
        }

        binding.ivBgBlack.visibility = View.GONE
        binding.btnAnswer.visibility = View.GONE
        binding.flAnotherQuestion.visibility = View.GONE

        setUpSpeech()
        viewModel.currentItem.observe(viewLifecycleOwner) { item ->
            updateQuestionUI(item, playSpeech = true)
        }

        viewModel.insertLogEvent.observe(viewLifecycleOwner) { event ->
            binding.includeNewRecord.layoutSend.isEnabled = true
            when (event.status_code) {
                NetworkStatusCode.ERROR_INSERT_LOG_FAILED_SPECIFIC, NetworkStatusCode.ERROR_ELDERLY_NOT_REGISTERED -> {
                    Toasty.error(activity as MainActivity, event.status.toString()).show()
                    findNavController().navigate(NewSpeechFragmentDirections.actionNewSpeechToMenuFragment())
                }

                NetworkStatusCode.SUCCESS -> {
                    activity?.let { activity ->
                        ResultDialog(activity).apply {
                            window?.requestFeature(Window.FEATURE_NO_TITLE)
                            setText(requireContext().getString(R.string.message_success_to_answer), requireContext().getString(R.string.message_check_answer), requireContext().getString(R.string.message_back_to_home))
                            setDialogListener(object : ResultDialog.DialogListener {
                                override fun checkAnswer() { // 답변 확인하기
                                    DWLog.d("답변 확인하기 버튼 클릭 ==> 답변 확인 UI")
                                    findNavController().navigate(NewSpeechFragmentDirections.actionNewSpeechFragmentToQuestiondetailFragment())
                                }

                                override fun moveHome() {
                                    findNavController().navigate(NewSpeechFragmentDirections.actionNewSpeechToMenuFragment())
                                }
                            })
                            setDismissListener(object : ResultDialog.DialogDismissListener {
                                override fun onDismiss() {}
                            })
                            show()
                        }
                    }
                }

                else -> {
                    RxBus.publish(RxEvent.destroyApp)
                }
            }
        }

        binding.btnStop.setOnClickListener {
            findNavController().navigate(
                NewSpeechFragmentDirections.actionNewSpeechToMenuFragment(),
                NavOptions.Builder()
                    .setPopUpTo(R.id.new_speech_fragment, true)
                    .build()
            )
        }

        binding.btnAnswer.setOnClickListener {
            binding.flTitle.visibility = View.GONE // 상단 "자서전 만들기" 타이틀 제거
            binding.btnAnswer.visibility = View.GONE // "답변하기" 마이크 버튼 제거
            binding.flAnotherQuestion.visibility = View.GONE // "다른 질문 보기" 마이크 버튼 제거
            binding.includeNewRecord.root.visibility = View.VISIBLE
            binding.tvChronometer.visibility = View.VISIBLE // 남은 시간
            binding.chronometer.visibility = View.VISIBLE // 01:00
        }

        // 신규 "질문 다시 듣기" 버튼 클릭
        binding.flQListen.setOnClickListener {
            viewModel.currentItem.value?.let { updateQuestionUI(it, true) }
            viewModel.extendAnswerTime()
        }


        // 신규 "답변 하기" 버튼 클릭
        binding.flQAnswer.setOnClickListener {
            binding.flTitle.visibility = View.GONE // 상단 typeName 타이틀 제거
            binding.flQListen.visibility = View.GONE
            binding.flQAnswer.visibility = View.GONE

            binding.tvQuestionTitle.visibility = View.VISIBLE
            binding.includeNewRecord.root.visibility = View.VISIBLE
            binding.tvChronometer.visibility = View.VISIBLE // 남은 시간
            binding.chronometer.visibility = View.VISIBLE // 01:00

            val params = binding.tvQuestion.layoutParams as ConstraintLayout.LayoutParams
            params.width = 976.dpToPx()
            params.height = 180.dpToPx()
            binding.tvQuestion.layoutParams = params

            binding.tvQuestion.apply {
                isSingleLine = false
                ellipsize = null
                maxLines = 2
                setPadding(paddingLeft, paddingTop, paddingRight, 20.dpToPx())
            }

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                binding.tvQuestion,
                50, // 최소 글자 크기 줄여줌
                90, // 최대 글자 크기
                1,
                TypedValue.COMPLEX_UNIT_SP
            )

            // 오토사이즈 설정
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                binding.tvQuestion,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )

            // 배경
            binding.tvQuestion.setBackgroundResource(R.drawable.speech_question_background)

            viewModel.extendAnswerTime() // 종료시간 연장
        }

        binding.includeNewRecord.chRecState.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isChecked) {
                binding.includeNewRecord.apply {
                    layoutMediaPlay.isEnabled = false
                    tvMediaPlay.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))
                    txtSend.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))
                    tvRecState.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))
                    tvRecState.text = getString(R.string.recording_end)
                    layoutSend.isEnabled = false
                }

                binding.chronometer.base = SystemClock.elapsedRealtime()
                binding.chronometer.start()
                binding.chronometer.setTextColor(ContextCompat.getColor(requireContext(), R.color.recordingTimerTextColor))
                binding.tvChronometer.setTextColor(ContextCompat.getColor(requireContext(), R.color.recordingTimerTextColor))

                startTimeMillis = SystemClock.elapsedRealtime()
                viewModel.startRecording()

                handler.postDelayed({
                    val elapsedTime = SystemClock.elapsedRealtime() - startTimeMillis
                    if (elapsedTime >= limitTimeMillis) {
                        stopRecording()
                    }
                }, limitTimeMillis)
            } else {
                DWLog.d("녹음 멈춤 ${viewModel.recordStatus}")
                if (viewModel.recordStatus == NewSpeechViewModel.RecordStatus.RECORDING)
                    stopRecording()
            }
        }

        // 재생 시작 버튼 선택
        binding.includeNewRecord.layoutMediaPlay.setOnClickListener {
            DWLog.e("Play Status :: ${viewModel.playStatus}")
            when (viewModel.playStatus) {
                NewSpeechViewModel.PlayStatus.INIT, NewSpeechViewModel.PlayStatus.STOP -> {
                    viewModel.playWavFile()
                    timeWhenPlayStopped = 0
                    binding.includeNewRecord.apply {
                        layoutMediaPlay.visibility = View.GONE
                        tvMediaPlay.visibility = View.GONE
                        layoutMediaStop.visibility = View.VISIBLE
                        txtMediaStop.visibility = View.VISIBLE

                        chRecState.isEnabled = false
                        tvRecState.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))

                        val alpha = chRecState.background
                        alpha.alpha = 70
                        layoutSend.isEnabled = false

                    }
                    binding.chronometer.base = SystemClock.elapsedRealtime() + offset
                    binding.chronometer.start()
                }

                else -> {
                    viewModel.resumeWavFile()
                    binding.includeNewRecord.apply {
                        layoutMediaPlay.visibility = View.GONE
                        tvMediaPlay.visibility = View.GONE
                        layoutMediaStop.visibility = View.VISIBLE
                        txtMediaStop.visibility = View.VISIBLE

                        chRecState.isEnabled = false
                        chRecState.background.alpha = 70
                        tvRecState.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))
                        layoutSend.isEnabled = false
                    }
                    binding.chronometer.base = SystemClock.elapsedRealtime() + timeWhenPlayStopped
                    binding.chronometer.start()
                }
            }
        }

        viewModel.onPlayCompleted = {
            DWLog.d("재생 완료 콜백 수신")
            timeWhenPlayStopped = 0
            binding.includeNewRecord.apply {
                layoutMediaStop.visibility = View.GONE
                txtMediaStop.visibility = View.GONE
                layoutMediaPlay.visibility = View.VISIBLE
                tvMediaPlay.visibility = View.VISIBLE

                chRecState.isEnabled = true
                tvRecState.setTextColor(Color.WHITE)
                val alpha = chRecState.background
                alpha.alpha = 255

                layoutSend.isEnabled = true
            }
            binding.chronometer.base = SystemClock.elapsedRealtime()
            binding.chronometer.stop()
        }

        /* 재생 중지 버튼 선택 */
        binding.includeNewRecord.layoutMediaStop.setOnClickListener {
            viewModel.pauseWavFile()
            binding.includeNewRecord.apply {
                layoutMediaStop.visibility = View.GONE
                txtMediaStop.visibility = View.GONE
                layoutMediaPlay.visibility = View.VISIBLE
                tvMediaPlay.visibility = View.VISIBLE

                chRecState.isEnabled = true
                chRecState.background.alpha = 255
                tvRecState.setTextColor(Color.WHITE)

                layoutSend.isEnabled = true
            }
            timeWhenPlayStopped = binding.chronometer.base - SystemClock.elapsedRealtime()
            binding.chronometer.stop()
        }

        /* 저장 버튼 선택 */
        binding.includeNewRecord.layoutSend.setOnClickListener {
            binding.includeNewRecord.layoutSend.isEnabled = false
            viewModel.stopRecording()
            viewModel.stopWavFile()
            viewModel.insertLog()
        }
    }

    private fun setUpSpeech() {
        viewModel.speechStatus.observe(viewLifecycleOwner) {
            changeStatus(it)
        }
    }

    private fun changeStatus(status: SpeechStatus) {
        DWLog.i("changeStatus animation == [$status]")
        when (status) {
            SpeechStatus.WAITING -> {
                binding.ivBgBlack.visibility = View.VISIBLE
                binding.btnAnswer.visibility = View.GONE

                binding.flSpeechStatus.visibility = View.GONE
                binding.flReading.visibility = View.GONE

                binding.flQAnswer.visibility = View.VISIBLE
                binding.flQListen.visibility = View.VISIBLE
            }

            SpeechStatus.SPEECH -> {
                binding.ivBgBlack.visibility = View.GONE
                binding.btnAnswer.visibility = View.GONE

                binding.flSpeechStatus.visibility = View.VISIBLE
                binding.flReading.visibility = View.VISIBLE

                binding.flQAnswer.visibility = View.GONE
                binding.flQListen.visibility = View.GONE
            }

            else -> {}
        }
    }

    private fun stopRecording() {
        binding.includeNewRecord.apply {
            chRecState.isChecked = false
            layoutMediaPlay.isEnabled = true
            tvMediaPlay.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))
            txtSend.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))
            layoutSend.isEnabled = true
            viewModel.stopRecording()
            if (viewModel.recordStatus == NewSpeechViewModel.RecordStatus.STOP) {
                tvRecState.text = getString(R.string.recording_start)
                tvRecState.setTextColor(Color.WHITE)
            }
        }
        binding.chronometer.stop()
        binding.chronometer.setTextColor(Color.WHITE)
        binding.tvChronometer.setTextColor(Color.WHITE)
    }

    private fun updateQuestionUI(item: Entry, playSpeech: Boolean) {
        binding.flSpeechStatus.visibility = View.VISIBLE
        binding.flReading.visibility = View.VISIBLE

        binding.ivBgBlack.visibility = View.GONE
        binding.flQListen.visibility = View.GONE
        binding.flQAnswer.visibility = View.GONE
        binding.tvQuestionTitle.visibility = View.GONE

        binding.tvQuestionTitle.text = item.typeName
        binding.tvQuestion.text = item.viewQuestion
        binding.tvTitleSpeech.text = item.typeName

        Glide.with(requireContext())
            .load(item.imgUrl)
            .placeholder(ContextCompat.getDrawable(requireContext(), R.color.transparent))
            .error(ContextCompat.getDrawable(requireContext(), R.drawable.item))
            .centerCrop()
            .into(binding.ivBg)

        if (playSpeech) {
            binding.root.post {
                viewModel.speech(item.question)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isRecording.value == true) {
            viewModel.stopRecording()
            viewModel.stopWavFile()
        }
        viewModel.disconnect()
    }


    companion object {

    }
}