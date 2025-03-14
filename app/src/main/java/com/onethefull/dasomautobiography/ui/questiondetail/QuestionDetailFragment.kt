package com.onethefull.dasomautobiography.ui.questiondetail

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.contents.dialog.PopupDialog
import com.onethefull.dasomautobiography.contents.dialog.ResponseEditDialog
import com.onethefull.dasomautobiography.contents.dialog.ResultDialog
import com.onethefull.dasomautobiography.databinding.FragmentQuestionDetailBinding
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog

/**
 * Created by sjw on 2025. 3. 7.
 */
class QuestionDetailFragment : Fragment() {
    private lateinit var binding: FragmentQuestionDetailBinding
    private var itemName: String? = null

    val viewModel: QuestionDetailViewModel by viewModels {
        InjectorUtils.provideQuestionDetailModelFactory(requireContext())
    }

    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemName = it.getString("itemName", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQuestionDetailBinding.inflate(inflater, container, false).apply { }
        viewModel.fetchDataFromSharedViewModel(sharedViewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarTitle.text = itemName

        sharedViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item != null) {
                DWLog.d("Received item [name]:: ${itemName},  [title]:: ${item.type}  ${item.sort}, ${item.id} [question]::${item.viewQuestion}")
                binding.tvNumber.text = item.sort
                binding.tvQuestion.text = item.viewQuestion
                viewModel.getLogDtl(item.logId.toString())
            } else {
                RxBus.publish(RxEvent.destroyApp)
            }
        }

        viewModel.transText.observe(viewLifecycleOwner) { transText ->
            if(viewModel.transText.value != "") {
                binding.tvAnswer.text = "답변 : $transText"
            }
        }

        viewModel.deleteEvent.observe(viewLifecycleOwner) {
            (activity as MainActivity).back()
        }

        viewModel.showDialog.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow) {
                activity?.let { activity ->
                    ResponseEditDialog(activity).apply {
                        window?.requestFeature(Window.FEATURE_NO_TITLE)
                        setDialogListener(object : ResponseEditDialog.DialogListener {
                            override fun checkAnswer() {
                                dismiss()
                            }
                            override fun moveHome() {
                                findNavController().navigate(QuestionDetailFragmentDirections.actionDetailFragmentToMenuFragment())
                            }
                        })
                        show()
                    }
                }
            }
        }

        binding.btnListen.setOnClickListener {
            viewModel.startSpeech(sharedViewModel.selectedItem.value?.viewQuestion.toString())
        }

        binding.btnAnswerListen.setOnClickListener {
            if (viewModel.answerAudioUrl.value != null || viewModel.answerAudioUrl.value != "") {
                viewModel.startUrlSpeech(viewModel.answerAudioUrl.value.toString())
            }
        }

        binding.btnDelete.setOnClickListener {
            activity?.let { activity ->
                PopupDialog(activity).apply {
                    window?.requestFeature(Window.FEATURE_NO_TITLE)
                    setText("답변을 삭제하시겠어요?", "저장된 답변이 삭제됩니다. \n새롭게 답변해주세요.")
                    setDialogListener(object : PopupDialog.DialogListener {
                        override fun delete() {
                            viewModel.deleteLog()
                        }
                    })
                    show()
                }
            }
        }

        binding.btnRetry.setOnClickListener {
            activity?.let { activity ->
                PopupDialog(activity).apply {
                    window?.requestFeature(Window.FEATURE_NO_TITLE)
                    setText("질문에 다시 답변하시겠어요?", "저장된 답변이 삭제됩니다. \n다시 답변해주세요.")
                    setDialogListener(object : PopupDialog.DialogListener {
                        override fun delete() {
                            dismiss()
                            binding.customToolbar.visibility = View.GONE
                            binding.layoutQuestionDetail.visibility = View.GONE
                            binding.layoutAnswerDetail.visibility = View.GONE
                            binding.layoutSelectDetail.visibility = View.GONE
                            binding.layoutRecording.visibility = View.VISIBLE

                            binding.ivRecording.visibility = View.VISIBLE
                            binding.ivRecordingEnd.visibility = View.GONE
                            binding.ivRecordingRestart.visibility = View.GONE
                        }
                    })
                    show()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnHome.setOnClickListener {
            RxBus.publish(RxEvent.destroyApp)
        }

        // 다시 답변 다이얼로그 화면
        binding.tvTitle.text = sharedViewModel.selectedItem.value?.viewQuestion // 질문 재세팅

        // 1초마다 TextView 자동 업데이트
        viewModel.timeLeft.observe(viewLifecycleOwner) { time ->
            binding.tvLeftTime.setTextColor(Color.parseColor("#FAFF5E"))
            binding.tvLeftTime.text = String.format("남은 시간 00:%02d", time)
        }
        binding.tvLeftTime.visibility = View.VISIBLE
        binding.tvLeftTime.setTextColor(Color.parseColor("#FFFFFF"))
        binding.tvLeftTime.text = "남은 시간 01:00"

        binding.ivRecording.setOnClickListener { // "답변 하기" 버튼 누르기 -> "답변 종료" 버튼 활성화, 나머지 비활성화
            binding.ivRecordingEnd.visibility = View.VISIBLE
            binding.ivRecordingRestart.visibility = View.GONE
            binding.ivRecording.visibility = View.GONE

            binding.tvRecording.text = "답변 종료"
            binding.tvRecording.setTextColor(Color.RED)
            binding.ivRecording.visibility = View.GONE

            binding.ivListen.isClickable = false
            binding.ivListen.setBackgroundResource(R.drawable.btn_rec_not_play_)
            binding.tvListen.setTextColor(Color.DKGRAY)

            binding.ivSave.isClickable = false
            binding.ivSave.setBackgroundResource(R.drawable.btn_rec_not_save_)
            binding.tvSave.setTextColor(Color.DKGRAY)

            viewModel.startTimer() // 타이머 시작
            viewModel.startRecording()
        }

        binding.ivRecordingEnd.setOnClickListener {  // "답변 종료" 버튼 누르기 -> "재답변하기" 버튼, "듣기", "저장"버튼 활성화
            binding.ivRecordingRestart.visibility = View.VISIBLE
            binding.ivRecording.visibility = View.GONE
            binding.ivRecordingEnd.visibility = View.GONE

            binding.tvRecording.text = "답변 하기"
            binding.tvRecording.setTextColor(Color.BLACK)

            binding.ivRecording.visibility = View.GONE
            binding.ivRecordingEnd.visibility = View.GONE

            binding.ivListen.isClickable = true
            binding.ivListen.setBackgroundResource(R.drawable.btn_rec_play_)
            binding.tvListen.setTextColor(Color.BLACK)


            binding.ivSave.isClickable = true
            binding.ivSave.setBackgroundResource(R.drawable.btn_rec_save_)
            binding.tvSave.setTextColor(Color.BLACK)


            binding.tvLeftTime.setTextColor(Color.parseColor("#FFFFFF"))
            viewModel.stopTimer() // 타이머 중지
            viewModel.stopRecording()
        }

        binding.ivRecordingRestart.setOnClickListener {
            binding.ivRecordingEnd.visibility = View.VISIBLE
            binding.ivRecording.visibility = View.GONE
            binding.ivRecordingRestart.visibility = View.GONE

            binding.tvRecording.text = "답변 종료"
            binding.tvRecording.setTextColor(Color.RED)
            binding.ivRecording.visibility = View.GONE

            binding.ivListen.isClickable = false
            binding.ivListen.setBackgroundResource(R.drawable.btn_rec_not_play_)
            binding.tvListen.setTextColor(Color.DKGRAY)

            binding.ivSave.isClickable = false
            binding.ivSave.setBackgroundResource(R.drawable.btn_rec_not_save_)
            binding.tvSave.setTextColor(Color.DKGRAY)

            viewModel.startRecording()
            viewModel.startTimer() // 다시 타이머 시작
        }

        binding.ivListen.setOnClickListener{
            viewModel.playWavFile()
        }

        binding.ivSave.setOnClickListener {
            viewModel.insertLog()
        }

        binding.ivCancel.setOnClickListener {
            binding.customToolbar.visibility = View.VISIBLE
            binding.layoutQuestionDetail.visibility = View.VISIBLE
            binding.layoutAnswerDetail.visibility = View.VISIBLE
            binding.layoutSelectDetail.visibility = View.VISIBLE
            binding.layoutRecording.visibility = View.GONE
        }
    }

}