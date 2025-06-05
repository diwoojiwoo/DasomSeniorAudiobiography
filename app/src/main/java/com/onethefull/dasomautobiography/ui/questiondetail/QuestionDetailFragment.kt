package com.onethefull.dasomautobiography.ui.questiondetail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.contents.dialog.PopupDialog
import com.onethefull.dasomautobiography.contents.dialog.ResponseEditDialog
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.databinding.FragmentQuestionDetailBinding
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.setOnSingleClickListener
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus

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

    private var currentAnswerIndex = 0

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constant.ACTION_STT_TEXT) {
                val logId = intent.getStringExtra(Constant.PARAM_LOG_ID) ?: return
                DWLog.d("BroadcastReceiver onReceive [logId] :: $logId")
                viewModel.getLogDtl(sharedViewModel.selectedItem.value?.autobiographyId.toString())
//                viewModel.getLogDtl(logId)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DWLog.d("onCreate")
        arguments?.let {
            itemName = it.getString("itemName", "")
        }
    }

    override fun onResume() {
        super.onResume()
        DWLog.d("onResume")
        initView()
        val filter = IntentFilter(Constant.ACTION_STT_TEXT)
        requireContext().registerReceiver(receiver, filter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQuestionDetailBinding.inflate(inflater, container, false).apply { }
        viewModel.fetchDataFromSharedViewModel(sharedViewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item != null) {
                DWLog.d("Received item [name]:: ${itemName}, [title]:: ${item.type}  ${item.sort}, ${item.autobiographyId} [question]::${item.viewQuestion}")
                binding.tvListenAnswer.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.icon_convert)

                binding.layoutAnswerDetail.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.new_answer_convert_background)
                binding.tvAnswer.text = "답변을 등록 중입니다."
                binding.tvAnswer.setTextColor(Color.WHITE)
                binding.toolbarTitle.text = item.typeName
                binding.tvQuestion.text = "질문 : " + item.viewQuestion
                viewModel.getLogDtl(item.autobiographyId.toString())
            } else {
                RxBus.publish(RxEvent.destroyApp)
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
                                binding.customToolbar.visibility = View.VISIBLE
                                binding.layoutQuestionDetail.visibility = View.VISIBLE
                                binding.layoutAnswerDetail.visibility = View.VISIBLE
                                binding.layoutSelectDetail.visibility = View.VISIBLE
                                updateAnswerDisplay()
                                binding.layoutRecording.visibility = View.GONE
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

        viewModel.logDtlEvent.observe(viewLifecycleOwner) { event ->
            binding.layoutAnswerDetail.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.new_answer_detail_background)
            binding.tvListenAnswer.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.btn_listen_checkbox)
            binding.tvAnswer.setTextColor(Color.BLACK)

            when (event.statusCode) {
                1001 -> {
                    Toasty.error(activity as MainActivity, event.message.toString()).show()
                    RxBus.publish(RxEvent.destroyApp)
                }

                -3 -> {
                    Toasty.error(activity as MainActivity, event.message.toString()).show()
                    RxBus.publish(RxEvent.destroyApp)
                }

                0 -> {
                    currentAnswerIndex = 0
                    updateAnswerDisplay()
                }

                else -> {
                    Toasty.error(activity as MainActivity, event.message ?: "알 수 없는 오류").show()
                    RxBus.publish(RxEvent.destroyApp)
                }
            }
        }

        viewModel.insertLogEvent.observe(viewLifecycleOwner) { event ->
            when (event.status_code) {
                -99, -3, -104 -> {
                    Toasty.error(activity as MainActivity, event.status.toString()).show()
                    binding.customToolbar.visibility = View.VISIBLE
                    binding.layoutQuestionDetail.visibility = View.VISIBLE
                    binding.layoutAnswerDetail.visibility = View.VISIBLE
                    binding.layoutSelectDetail.visibility = View.VISIBLE
                    updateAnswerDisplay()
                    binding.layoutRecording.visibility = View.GONE
                }
            }
        }

        binding.btnLeft.setOnClickListener {
            if (currentAnswerIndex > 0) {
                currentAnswerIndex--
                updateAnswerDisplay()
            }
        }

        binding.btnRight.setOnClickListener {
            val answers = viewModel.logDtlEvent.value?.autobiographyMap?.list ?: emptyList()
            if (currentAnswerIndex < answers.size - 1) {
                currentAnswerIndex++
                updateAnswerDisplay()
            }
        }

        /**
         * 문제 듣기 버튼 클릭 리스너
         * */
        binding.tvListenQuestion.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.startSpeech(sharedViewModel.selectedItem.value?.viewQuestion.toString())
            }
        }

        /**
         * 답변 듣기 버튼 클릭 리스너
         * */
        binding.tvListenAnswer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val audioUrl = viewModel.answerAudioUrl.value
                if (!audioUrl.isNullOrBlank()) {
                    viewModel.startUrlSpeech(audioUrl)
                } else {
                    DWLog.e("Audio URL is null or blank")
                    viewModel.startSpeech(viewModel.transText.value.toString())
                }
            }
        }

        viewModel.speechStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                SpeechStatus.SPEECH -> {
//                  binding.tvListenQuestion.background = ContextCompat.getDrawable(requireContext(), R.drawable.icon_listen_active)
//                  binding.tvListenAnswer.background = ContextCompat.getDrawable(requireContext(), R.drawable.icon_listen_active)
                }

                else -> {
                    binding.tvListenQuestion.isChecked = false
                    binding.tvListenAnswer.isChecked = false
                }
            }
        }

        /**
         * "답변 삭제" 버튼 클릭 리스너
         * */
        binding.btnDelete.setOnClickListener {
            activity?.let { activity ->
                binding.btnRight.visibility = View.GONE
                binding.btnLeft.visibility = View.GONE
                val logId = viewModel.logId.value
                PopupDialog(activity).apply {
                    window?.requestFeature(Window.FEATURE_NO_TITLE)
                    setText("저장된 답변을 삭제합니다.", "답변을 다시 등록해주세요.")
                    setDialogListener(object : PopupDialog.DialogListener {
                        override fun delete() {
                            viewModel.deleteLog(logId.toString())
                        }
                    })
                    show()
                }
            }
        }

        /**
         * "추가 답변" 버튼 클릭 리스너
         */
        binding.btnRetry.setOnClickListener {
            binding.customToolbar.visibility = View.GONE
            binding.layoutQuestionDetail.visibility = View.GONE
            binding.layoutAnswerDetail.visibility = View.GONE
            binding.layoutSelectDetail.visibility = View.GONE
            binding.btnRight.visibility = View.GONE
            binding.btnLeft.visibility = View.GONE
            binding.layoutRecording.visibility = View.VISIBLE
        }

        /**
         * "다시 답변 버튼 클릭 리스너"
         */
        binding.btnBack.setOnClickListener {
            findNavController().navigate(QuestionDetailFragmentDirections.actionDetailFragmentToMenuFragment())
        }

        binding.btnHome.setOnClickListener {
            RxBus.publish(RxEvent.destroyApp)
        }

        // 다시 답변 다이얼로그 화면
        binding.tvTitle.text = sharedViewModel.selectedItem.value?.viewQuestion // 질문 재세팅


        viewModel.timeLeft.observe(viewLifecycleOwner) { time ->
            if (time < 60) {
                binding.tvLeftTime.setTextColor(Color.parseColor("#ff6363"))
                binding.tvLeftTime.text = String.format("남은 시간 00:%02d", time)
            }
        }

        binding.cbRecording.setOnCheckedChangeListener { cb, isChecked ->
            if (cb.isChecked) {
//                DWLog.d("답변 하기")
                // 듣기 버튼 상태
                binding.btnPlay.isEnabled = false
                binding.btnPlay.isChecked = false
                binding.tvPlay.setTextColor(Color.DKGRAY)

                // 저장 버튼 상태
                binding.btnSave.isEnabled = false
                binding.tvSave.setTextColor(Color.DKGRAY)

                // 녹음 텍스트
                binding.tvRecording.text = "답변 종료"
                binding.tvRecording.setTextColor(Color.parseColor("#ff6363"))

                viewModel.startTimer() // 타이머 시작
                viewModel.startRecording()
            } else {
//                DWLog.d("답변 종료")
                // 듣기 버튼 상태 -> 체크되지 않음 && 활성화 상태
                binding.btnPlay.isEnabled = true
                binding.btnPlay.isChecked = false
                binding.tvPlay.setTextColor(Color.GRAY)

                binding.btnSave.isEnabled = true
                binding.tvSave.setTextColor(Color.GRAY)

                binding.tvRecording.text = "답변 하기"
                binding.tvRecording.setTextColor(Color.BLACK)

                viewModel.resetTimer()
                viewModel.stopRecording()
            }
        }

        binding.btnPlay.setOnCheckedChangeListener { cb, isChecked ->
            if (cb.isChecked) {
//                DWLog.e("녹음된 음성 듣기 중")

                binding.btnPlay.isEnabled = true
                binding.tvPlay.setTextColor(Color.BLACK)
                binding.tvPlay.text = "중지"

                binding.cbRecording.isEnabled = false
                binding.tvRecording.text = "답변 하기"
                binding.tvRecording.setTextColor(Color.GRAY)

                binding.btnSave.isEnabled = false
                binding.tvSave.setTextColor(Color.GRAY)

                viewModel.startTimer()
                viewModel.playWavFile()
            } else {
//                DWLog.e("음성 듣기 중 정지 누름")
                resetPlayUI()
            }
        }


        viewModel.isPlaying.observe(viewLifecycleOwner) {
            if (!it) {
                resetPlayUI()
            }
        }

        binding.btnSave.setOnSingleClickListener {
            viewModel.insertLog()
        }

        binding.ivCancel.setOnClickListener {
            binding.customToolbar.visibility = View.VISIBLE
            binding.layoutQuestionDetail.visibility = View.VISIBLE
            binding.layoutAnswerDetail.visibility = View.VISIBLE
            binding.layoutSelectDetail.visibility = View.VISIBLE
            binding.layoutRecording.visibility = View.GONE

            viewModel.stopWavFile()
            viewModel.stopRecording()
        }
    }

    private fun initView() {
        // 듣기 버튼 상태 (체크되지 않음 && 비활성화 상태)
        binding.btnPlay.isEnabled = false
        binding.btnPlay.isChecked = false
        binding.tvPlay.setTextColor(Color.GRAY)

        // 저장 버튼 상태
        binding.btnSave.isEnabled = false
        binding.tvSave.setTextColor(Color.GRAY)

        // 녹음 텍스트
        binding.cbRecording.isChecked = false
        binding.tvRecording.text = "답변 하기"
        binding.tvRecording.setTextColor(Color.parseColor("#333333"))
    }

    private fun updateAnswerDisplay() {
        val event = viewModel.logDtlEvent.value ?: return
        val answers = event.autobiographyMap?.list ?: emptyList()

        if (answers.isNotEmpty() && viewModel.transText.value != "") {
            val currentAnswer = answers[currentAnswerIndex]
            binding.tvAnswer.text = "답변 : ${currentAnswer.transText}"
            viewModel.setAnswerAudioUrl(currentAnswer.answerAudioUrl ?: "")
            viewModel.setLogId((currentAnswer.autobiographyLogId ?: -1).toString())
            if (answers.size == 1) {
                binding.btnLeft.visibility = View.GONE
                binding.btnRight.visibility = View.GONE
            } else {
                binding.btnLeft.visibility = if (currentAnswerIndex > 0) View.VISIBLE else View.GONE
                binding.btnRight.visibility = if (currentAnswerIndex < answers.size - 1) View.VISIBLE else View.GONE
            }

            // 추가답변 버튼 텍스트 설정
            if (answers.size > 1) {
                val current = currentAnswerIndex + 1
                val total = answers.size
                binding.tvRetry.text = "추가답변 ($current/$total)"
            } else {
                binding.tvRetry.text = "추가답변"
            }

        } else {
            // 답변이 없을 때 기본 텍스트
            binding.tvRetry.text = "추가답변"
        }
    }

    private fun resetPlayUI() {
        // 듣기
        binding.btnPlay.isEnabled = true
        binding.btnPlay.isChecked = false
        binding.tvPlay.setTextColor(Color.BLACK)
        binding.tvPlay.text = "듣기"

        // 저장
        binding.btnSave.isEnabled = true
        binding.tvSave.setTextColor(Color.BLACK)

        // 녹음
        binding.cbRecording.isEnabled = true
        binding.tvRecording.text = "답변 하기"
        binding.tvRecording.setTextColor(Color.parseColor("#333333"))

        viewModel.resetTimer()
        viewModel.pauseWavFile()
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
        requireContext().unregisterReceiver(receiver)
    }

}