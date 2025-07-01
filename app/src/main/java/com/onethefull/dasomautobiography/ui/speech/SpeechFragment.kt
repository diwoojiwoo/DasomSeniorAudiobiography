package com.onethefull.dasomautobiography.ui.speech

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.contents.dialog.ResultDialog
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.databinding.FragmentSpeechBinding
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus
import com.onethefull.wonderfulrobotmodule.ext.dasomLanguageCodeValue
import androidx.core.graphics.toColorInt
import com.onethefull.wonderfulrobotmodule.robot.BaseRobotController


class SpeechFragment : Fragment() {
    private lateinit var binding: FragmentSpeechBinding
    val viewModel: SpeechViewModel by viewModels {
        InjectorUtils.provideSpeechViewModelFactory(requireContext())
    }
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val limitTimeMillis: Long = 60_000L
    private var startTimeMillis: Long = 0L
    private val offset = 1L
    private var timeWhenPlayStopped: Long = 0
    private val handler =  Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        binding.includeNewRecord.apply {
            btnMediaPlay.isEnabled = false
            save.isEnabled = false
            chRecState.isChecked = false
            chRecState.isEnabled = true
            txtRecState.text = resources.getString(R.string.recording_start)

            val alpha = chRecState.background
            alpha.alpha = 255
        }
        binding.chronometer.base = SystemClock.elapsedRealtime()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpeechBinding.inflate(inflater, container, false).apply {}
        viewModel.fetchDataFromSharedViewModel(sharedViewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val language = App.instance.getLocale()?.dasomLanguageCodeValue() ?: "ko"
        when (language) {
            "en-US" -> {
                binding.tvTitleSpeech.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                binding.tvQuestionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
//                binding.includeRecordStart.tvRecordingStart.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
//                binding.includeRecordRestart.tvRestartAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
//                binding.includeRecordStop.tvRecordingStop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            }

            else -> {
                binding.tvTitleSpeech.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
                binding.tvQuestionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
//                binding.includeRecordStart.tvRecordingStart.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
//                binding.includeRecordRestart.tvRestartAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
//                binding.includeRecordStop.tvRecordingStop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            }
        }

        binding.ivBgBlack.visibility = View.GONE
        binding.btnAnswer.visibility = View.GONE
        binding.flAnotherQuestion.visibility = View.GONE

        setUpSpeech()
        viewModel.currentItem.observe(viewLifecycleOwner) { item ->
            binding.tvQuestionTitle.text = item.typeName
            binding.tvQuestion.text = item.viewQuestion
            Glide.with(requireContext())
                .load(item.imgUrl)
                .placeholder(ContextCompat.getDrawable(requireContext(), R.color.transparent))
                .error(ContextCompat.getDrawable(requireContext(), R.drawable.item))
                .centerCrop()
                .into(binding.ivBg)
            viewModel.speech(item.question)
        }

        viewModel.insertLogEvent.observe(viewLifecycleOwner) { event ->
            binding.includeNewRecord.save.isEnabled = true
            when (event.status_code) {
                -99, -3 -> {
                    Toasty.error(activity as MainActivity, event.status.toString()).show()
                    findNavController().navigate(SpeechFragmentDirections.actionSpeechToMenuFragment())
                }

                0 -> {
                    activity?.let { activity ->
                        ResultDialog(activity).apply {
                            window?.requestFeature(Window.FEATURE_NO_TITLE)
                            setText(requireContext().getString(R.string.message_success_to_answer), requireContext().getString(R.string.message_check_answer), requireContext().getString(R.string.message_back_to_home))
                            setDialogListener(object : ResultDialog.DialogListener {
                                override fun checkAnswer() { // 답변 확인하기
                                    DWLog.d("답변 확인하기 버튼 클릭 ==> 답변 확인 UI")
                                    findNavController().navigate(SpeechFragmentDirections.actionSpeechFragmentToQuestiondetailFragment())
                                }

                                override fun moveHome() {
                                    findNavController().navigate(SpeechFragmentDirections.actionSpeechToMenuFragment())
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
            findNavController().navigate(SpeechFragmentDirections.actionSpeechToMenuFragment())
        }

        binding.btnAnswer.setOnClickListener {
            binding.flTitle.visibility = View.GONE // 상단 "자서전 만들기" 타이틀 제거
            binding.btnAnswer.visibility = View.GONE // "답변하기" 마이크 버튼 제거
            binding.flAnotherQuestion.visibility = View.GONE // "다른 질문 보기" 마이크 버튼 제거
            binding.includeNewRecord.root.visibility = View.VISIBLE
            binding.tvChronometer.visibility = View.VISIBLE // 남은 시간
            binding.chronometer.visibility = View.VISIBLE // 01:00
        }

        binding.includeNewRecord.chRecState.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isChecked) {
                binding.includeNewRecord.apply {
                    btnMediaPlay.isEnabled = false
                    txtMediaPlay.setTextColor(Color.DKGRAY)
                    txtSend.setTextColor(Color.DKGRAY)
                    txtRecState.text = getString(R.string.recording_end)

                    save.isEnabled = false
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
                stopRecording()
            }
        }

        /* 재생 시작 버튼 선택 */
        binding.includeNewRecord.btnMediaPlay.setOnClickListener {
            DWLog.e("Play Status :: ${viewModel.playStatus}")
            when (viewModel.playStatus) {
                SpeechViewModel.PlayStatus.INIT, SpeechViewModel.PlayStatus.STOP -> {
                    viewModel.playWavFile()
                    timeWhenPlayStopped = 0
                    binding.includeNewRecord.apply {
                        btnMediaPlay.visibility = View.GONE
                        txtMediaPlay.visibility = View.GONE
                        btnMediaStop.visibility = View.VISIBLE
                        txtMediaStop.visibility = View.VISIBLE

                        chRecState.isEnabled = false
                        val alpha = chRecState.background
                        alpha.alpha = 70
                        save.isEnabled = false

                    }
                    binding.chronometer.base = SystemClock.elapsedRealtime() + offset
                    binding.chronometer.start()
                }
                else -> {
                    viewModel.resumeWavFile()
                    binding.includeNewRecord.apply {
                        btnMediaPlay.visibility = View.GONE
                        txtMediaPlay.visibility = View.GONE
                        btnMediaStop.visibility = View.VISIBLE
                        txtMediaStop.visibility = View.VISIBLE

                        chRecState.isEnabled = false
                        chRecState.background.alpha = 70
                        save.isEnabled = false
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
                btnMediaStop.visibility = View.GONE
                txtMediaStop.visibility = View.GONE
                btnMediaPlay.visibility = View.VISIBLE
                txtMediaPlay.visibility = View.VISIBLE

                chRecState.isEnabled = true
                val alpha = chRecState.background
                alpha.alpha = 255

                save.isEnabled = true
            }
            binding.chronometer.base = SystemClock.elapsedRealtime()
            binding.chronometer.stop()
        }

        /* 재생 중지 버튼 선택 */
        binding.includeNewRecord.btnMediaStop.setOnClickListener {
            viewModel.pauseWavFile()
            binding.includeNewRecord.apply {
                btnMediaStop.visibility = View.GONE
                txtMediaStop.visibility = View.GONE
                btnMediaPlay.visibility = View.VISIBLE
                txtMediaPlay.visibility = View.VISIBLE

                chRecState.isEnabled = true
                chRecState.background.alpha = 255
                save.isEnabled = true
            }
            timeWhenPlayStopped = binding.chronometer.base - SystemClock.elapsedRealtime()
            binding.chronometer.stop()
        }

        /* 저장 버튼 선택 */
        binding.includeNewRecord.save.setOnClickListener {
            binding.includeNewRecord.save.isEnabled = false
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
                binding.flAnotherQuestion.visibility = View.GONE

                binding.flTitle.visibility = View.GONE // 상단 "자서전 만들기" 타이틀 제거
                binding.flAnotherQuestion.visibility = View.GONE // "다른 질문 보기" 마이크 버튼 제거
                binding.includeNewRecord.root.visibility = View.VISIBLE
            }

            SpeechStatus.SPEECH -> {
                binding.ivBgBlack.visibility = View.GONE
                binding.btnAnswer.visibility = View.GONE
                binding.flAnotherQuestion.visibility = View.GONE
                binding.includeNewRecord.root.visibility = View.GONE
            }

            else -> {   }
        }
    }

    private fun stopRecording() {
        binding.includeNewRecord.apply {
            chRecState.isChecked = false
            btnMediaPlay.isEnabled = true
            txtMediaPlay.setTextColor(Color.WHITE)
            txtSend.setTextColor(Color.WHITE)
            save.isEnabled = true
            viewModel.stopRecording()
            if (viewModel.recordStatus == SpeechViewModel.RecordStatus.STOP) {
                txtRecState.text = getString(R.string.recording_start)
            }
        }
        binding.chronometer.stop()
        binding.chronometer.setTextColor(Color.WHITE)
        binding.tvChronometer.setTextColor(Color.WHITE)
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