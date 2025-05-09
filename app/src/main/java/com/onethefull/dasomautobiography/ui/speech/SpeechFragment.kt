package com.onethefull.dasomautobiography.ui.speech

import android.graphics.Color
import android.os.Bundle
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
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.contents.dialog.ResultDialog
import com.onethefull.dasomautobiography.databinding.FragmentSpeechBinding
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus


class SpeechFragment : Fragment() {
    private lateinit var binding: FragmentSpeechBinding
    val viewModel: SpeechViewModel by viewModels {
        InjectorUtils.provideSpeechViewModelFactory(requireContext())
    }
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.ivBgBlack.visibility = View.GONE
        binding.btnAnswer.visibility = View.GONE
        binding.flAnotherQuestion.visibility = View.GONE
        binding.includeRecordStop.root.visibility = View.GONE

        setUpSpeech()
        sharedViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item != null) {
                DWLog.d("Received item [title]:: ${item.typeName}  ${item.sort}, [question]::${item.viewQuestion}")
                binding.tvQuestionTitle.text = "${item.typeName}  ${item.sort}"
                binding.tvQuestion.text = item.viewQuestion
                Glide.with(requireContext())
                    .load(item.imgUrl)
                    .placeholder(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_popup_sync))
                    .error(ContextCompat.getDrawable(requireContext(), R.color.background_color))
                    .into(binding.ivBg)
                viewModel.speech(item.viewQuestion)
            } else {
                DWLog.e("Received null item")
                RxBus.publish(RxEvent.destroyApp)
            }
        }

        viewModel.isRecording.observe(viewLifecycleOwner) { isRecording ->
            DWLog.e("isRecording ??? $isRecording")
            if (isRecording) { // 답변 중 화면
                binding.includeRecordStart.root.visibility = View.GONE
                binding.includeRecordStop.root.visibility = View.VISIBLE
                binding.includeRecordRestart.root.visibility = View.GONE
            } else { // 답변 종료 후 화면
                binding.includeRecordStart.root.visibility = View.GONE
                binding.includeRecordStop.root.visibility = View.GONE
                binding.includeRecordRestart.root.visibility = View.VISIBLE
            }
        }

        // 1초마다 TextView 자동 업데이트
        viewModel.timeLeft.observe(viewLifecycleOwner) { time ->
            binding.tvLeftTime.setTextColor(Color.parseColor("#FAFF5E"))
            binding.tvLeftTime.text = String.format("남은 시간 00:%02d", time)
        }

        viewModel.showDialog.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow) {
                activity?.let { activity ->
                    ResultDialog(activity).apply {
                        window?.requestFeature(Window.FEATURE_NO_TITLE)
                        setText("자서전 내용이 저장되었습니다!", "답변 확인하기", "처음으로 돌아가기")
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
        }

        binding.btnAnswer.setOnClickListener {
            binding.flTitle.visibility = View.GONE // 상단 "자서전 만들기" 타이틀 제거
            binding.btnAnswer.visibility = View.GONE // "답변하기" 마이크 버튼 제거
            binding.flAnotherQuestion.visibility = View.GONE // "다른 질문 보기" 마이크 버튼 제거
            binding.includeRecordStart.root.visibility = View.VISIBLE // "답변 시작" 활성화 화면 보이기
            binding.includeRecordStop.root.visibility = View.GONE // "답변 종료" 화면 숨김

            binding.tvLeftTime.visibility = View.VISIBLE
            binding.tvLeftTime.setTextColor(Color.parseColor("#FFFFFF"))
            binding.tvLeftTime.text = "남은 시간 01:00"
        }

        binding.includeRecordStart.start.setOnClickListener { // "답변 시작" 버튼 클릭
            viewModel.startRecording() // 녹음 시작
            viewModel.startTimer() // 타이머 시작
        }

        binding.includeRecordStop.stop.setOnClickListener { // "답변 종료" 버튼 클릭
            binding.tvLeftTime.setTextColor(Color.parseColor("#FFFFFF"))
            viewModel.stopRecording() // 녹음 종료
            viewModel.stopTimer() // 타이머 중지
        }

        binding.includeRecordRestart.restart.setOnClickListener { // "다시 답변하기" 버튼 클릭
            viewModel.startRecording()
            viewModel.startTimer() // 다시 타이머 시작
        }

        binding.includeRecordRestart.play.setOnClickListener { // 재생하기 버튼 클릭
            viewModel.playWavFile()
        }

        binding.includeRecordRestart.save.setOnClickListener { // 저장하기 버튼 클릭
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
                binding.btnAnswer.visibility = View.VISIBLE
                binding.flAnotherQuestion.visibility = View.GONE
            }

            SpeechStatus.SPEECH -> {
                binding.ivBgBlack.visibility = View.GONE
                binding.btnAnswer.visibility = View.GONE
                binding.flAnotherQuestion.visibility = View.GONE
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