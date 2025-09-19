package com.onethefull.dasomautobiography.ui.questiondetail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.App
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.MainViewModel
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.contents.dialog.FullTextDialog
import com.onethefull.dasomautobiography.contents.dialog.PopupDialog
import com.onethefull.dasomautobiography.contents.dialog.ResponseEditDialog
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.databinding.FragmentQuestionDetailBinding
import com.onethefull.dasomautobiography.ui.speech.SpeechViewModel
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.MenuItemToEntryMapper
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.network.NetworkStatusCode
import com.onethefull.dasomautobiography.utils.setOnSingleClickListener
import com.onethefull.dasomautobiography.utils.speech.SpeakingTarget
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus
import com.onethefull.wonderfulrobotmodule.ext.dasomLanguageCodeValue

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

    var isFromBroadcast = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constant.ACTION_STT_TEXT) {
                val logId = intent.getStringExtra(Constant.PARAM_LOG_ID) ?: return
                DWLog.d("BroadcastReceiver onReceive [logId] :: $logId")
                isFromBroadcast = true
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

        val language = App.instance.getLocale()?.dasomLanguageCodeValue() ?: Constant.KO
        when (language) {
            "ko-KR" -> {
                binding.tvLeftTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                binding.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            }

            else -> {
                binding.tvLeftTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
                binding.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            }
        }

        sharedViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item != null) {
                DWLog.d("Received item [name]:: ${itemName}, [title]:: ${item.type}  ${item.sort}, ${item.autobiographyId} [question]::${item.viewQuestion}")
                binding.tvListenAnswer.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.icon_convert)

                binding.layoutAnswerDetail.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.new_answer_convert_background)
                binding.tvAnswer.text = requireContext().getString(R.string.message_registering_answer)
                binding.tvAnswer.setTextColor(Color.WHITE)
                binding.toolbarTitle.text = item.typeName
                binding.tvQuestion.text = item.viewQuestion
                viewModel.getLogDtl(item.autobiographyId.toString())
            } else {
                RxBus.publish(RxEvent.destroyApp)
            }
        }

        viewModel.deleteEvent.observe(viewLifecycleOwner) { event ->
            binding.progressBar.visibility = View.GONE
            when (event) {
                NetworkStatusCode.SUCCESS -> {
                    val list = viewModel.logDtlEvent.value?.autobiographyMap?.list
                    if (list.isNullOrEmpty()) {
                        val selectedEntry =
                            sharedViewModel.selectedItem.value?.let { selected ->
                                Entry(
                                    autobiographyId = 0,
                                    audioUrl = "",
                                    transText = "",
                                    imgUrl = "",
                                    question = "",
                                    answerYn = "",
                                    sort = "",
                                    type = selected.type,
                                    typeName = selected.typeName,
                                    viewQuestion = "",
                                )
                            } ?: Entry(
                                // null일 때 기본값
                                autobiographyId = 0,
                                audioUrl = "",
                                transText = "",
                                imgUrl = "",
                                question = "",
                                answerYn = "",
                                sort = "",
                                type = "",
                                typeName = "",
                                viewQuestion = "",
                            )
                        findNavController().navigate(QuestionDetailFragmentDirections.actionDetailFragmentToListFragment(selectedEntry))
                    } else {
                        currentAnswerIndex = 0
                        updateAnswerDisplay()
                    }
                }

                NetworkStatusCode.ERROR_ELDERLY_NOT_REGISTERED -> {
                    Toasty.error(requireContext(), requireContext().getString(R.string.message_not_registration_elderly)).show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }

                else -> {
                    RxBus.publish(RxEvent.destroyApp)
                }
            }
        }

        viewModel.logDtlEvent.observe(viewLifecycleOwner) { event ->
            when (event.statusCode) {
                NetworkStatusCode.ERROR_ELDERLY_INFO_NOT_EXIST -> {
                    Toasty.error(activity as MainActivity, event.message.toString()).show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }

                NetworkStatusCode.ERROR_ELDERLY_NOT_REGISTERED -> {
                    Toasty.error(activity as MainActivity, event.message.toString()).show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }

                NetworkStatusCode.SUCCESS -> {
                    val answers = event.autobiographyMap?.list ?: emptyList()
                    currentAnswerIndex = if (isFromBroadcast) {
                        isFromBroadcast = false
                        answers.lastIndex
                    } else {
                        0
                    }
                    updateAnswerDisplay()
                }

                else -> {
                    Toasty.error(activity as MainActivity, event.message ?: "알 수 없는 오류").show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }
            }
        }

        viewModel.insertLogEvent.observe(viewLifecycleOwner) { event ->
            binding.btnSave.isEnabled = true
            when (event.status_code) {
                NetworkStatusCode.ERROR_INSERT_LOG_FAILED_SPECIFIC,
                NetworkStatusCode.ERROR_ELDERLY_NOT_REGISTERED,
                NetworkStatusCode.ERROR_SOME_SPECIFIC_ISSUE_NEGATIVE_104 -> {
                    Toasty.error(activity as MainActivity, event.status.toString()).show()
                    binding.customToolbar.visibility = View.VISIBLE
                    binding.layoutQuestionDetail.visibility = View.VISIBLE
                    binding.layoutAnswerDetail.visibility = View.VISIBLE
                    binding.layoutSelectDetail.visibility = View.VISIBLE
                    updateAnswerDisplay()
                    binding.layoutRecording.visibility = View.GONE
                }

                NetworkStatusCode.SUCCESS -> {
                    activity?.let { activity ->
                        ResponseEditDialog(activity).apply {
                            window?.requestFeature(Window.FEATURE_NO_TITLE)
                            setDialogListener(object : ResponseEditDialog.DialogListener {
                                override fun checkAnswer() {
                                    binding.customToolbar.visibility = View.VISIBLE
                                    binding.layoutQuestionDetail.visibility = View.VISIBLE
                                    binding.layoutAnswerDetail.visibility = View.VISIBLE
                                    binding.layoutSelectDetail.visibility = View.VISIBLE
                                    updateAnswerDisplay()
                                    binding.layoutRecording.visibility = View.GONE
                                }

                                override fun moveHome() {
                                    findNavController().navigate(
                                        QuestionDetailFragmentDirections.actionDetailFragmentToMenuFragment()
                                    )
                                }
                            })
                            setDismissListener(object : ResponseEditDialog.DialogDismissListener {
                                override fun onDismiss() {
                                    DWLog.d("20초 뒤 자동 onDismiss 호출")
                                    findNavController().navigate(QuestionDetailFragmentDirections.actionDetailFragmentToMenuFragment())
                                }
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
         * 문제 듣기 버튼 전체 클릭 리스너
         * */
        binding.layoutQuestionDetail.setOnSingleClickListener {
            if (viewModel.speechStatus.value == null || viewModel.speechStatus.value == SpeechStatus.WAITING) {
                viewModel.startSpeech(sharedViewModel.selectedItem.value?.viewQuestion.toString())
                viewModel.setSpeakingTarget(SpeakingTarget.QUESTION)
            } else {
                DWLog.d("발화 중, 실행 안함")
            }
        }


        viewModel.speechStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                SpeechStatus.SPEECH -> {
                    when (viewModel.speakingTarget.value) {
                        SpeakingTarget.QUESTION -> {
                            binding.tvListenQuestion.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_play_pause_normal)
                            binding.layoutQuestionDetail.background = ContextCompat.getDrawable(requireContext(), R.drawable.new_answer_detail_background_active)
                            binding.tvSpeech.text = requireContext().getString(R.string.text_reading_your_question)
                        }

                        SpeakingTarget.ANSWER -> {
                            binding.tvListenAnswer.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_play_pause_normal)
                            binding.layoutAnswerDetail.background = ContextCompat.getDrawable(requireContext(), R.drawable.new_answer_detail_background_active)
                        }

                        else -> {}
                    }

                    binding.flSpeechStatus.visibility = View.VISIBLE
                    binding.btnRetry.background = ContextCompat.getDrawable(requireContext(), R.drawable.add_anwser_background_inactive)
                    binding.tvRetry.isClickable = false
                    binding.tvRetry.isFocusable = false
                    binding.tvRetry.isFocusableInTouchMode = false
                    binding.btnDelete.isEnabled = false
                    binding.btnDelete.isClickable = false

                    binding.tvAnswerStatus.apply {
                        alpha = 0.5f
                        isEnabled = false
                        isClickable = false
                    }

                    binding.btnLeft.apply {
                        alpha = 0.5f
                        isEnabled = false
                        isClickable = false
                    }

                    binding.btnRight.apply {
                        alpha = 0.5f
                        isEnabled = false
                        isClickable = false
                    }
                }

                else -> {
                    when (viewModel.speakingTarget.value) {
                        SpeakingTarget.QUESTION -> {
                            binding.tvListenQuestion.background = ContextCompat.getDrawable(requireContext(), R.drawable.icon_listen)
                            binding.layoutQuestionDetail.background = ContextCompat.getDrawable(requireContext(), R.drawable.new_answer_detail_background)
                        }

                        SpeakingTarget.ANSWER -> {
                            binding.tvListenAnswer.background = ContextCompat.getDrawable(requireContext(), R.drawable.icon_listen)
                            binding.layoutAnswerDetail.background = ContextCompat.getDrawable(requireContext(), R.drawable.new_answer_detail_background)
                        }

                        else -> {}
                    }
                    binding.flSpeechStatus.visibility = View.GONE
                    binding.btnRetry.background = ContextCompat.getDrawable(requireContext(), R.drawable.add_anwser_background)
                    binding.tvRetry.isClickable = true
                    binding.tvRetry.isFocusable = true
                    binding.tvRetry.isFocusableInTouchMode = true
                    binding.btnDelete.isEnabled = true
                    binding.btnDelete.isClickable = true

                    binding.tvAnswerStatus.apply {
                        alpha = 1f
                        isEnabled = true
                        isClickable = true
                    }

                    binding.btnLeft.apply {
                        alpha = 1f
                        isEnabled = true
                        isClickable = true
                    }

                    binding.btnRight.apply {
                        alpha = 1f
                        isEnabled = true
                        isClickable = true
                    }
                }
            }
        }

        /**
         * "답변 삭제" 버튼 클릭 리스너
         * */
        binding.btnDelete.setOnClickListener {
            activity?.let { activity ->
                val targetLogId = viewModel.logId.value?.toIntOrNull()
                binding.progressBar.visibility = View.VISIBLE
                PopupDialog(activity).apply {
                    window?.requestFeature(Window.FEATURE_NO_TITLE)
                    setText(requireContext().getString(R.string.message_title_remove_answer), requireContext().getString(R.string.message_content_remove_answer))
                    setDialogListener(object : PopupDialog.DialogListener {
                        override fun delete() {
                            viewModel.deleteLog(targetLogId.toString())
                            if (targetLogId != null) {
                                viewModel.removeLogById(targetLogId)
                            }
                        }

                        override fun cancel() {
                            binding.progressBar.visibility = View.GONE
                        }
                    })
                    show()
                }
            }
        }

        /**
         * "추가 답변" 버튼 클릭 리스너
         */
        binding.tvRetry.setOnClickListener {
            binding.customToolbar.visibility = View.GONE
            binding.layoutQuestionDetail.visibility = View.GONE
            binding.layoutAnswerDetail.visibility = View.GONE
            binding.layoutSelectDetail.visibility = View.GONE
            binding.layoutRecording.visibility = View.VISIBLE

            binding.btnLeft.visibility = View.GONE
            binding.btnRight.visibility = View.GONE

            initView()
            viewModel.stopRecording()
            viewModel.stopWavFile()
            viewModel.resetTimer()
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
        binding.tvContent.text = sharedViewModel.selectedItem.value?.viewQuestion // 질문 재세팅

        viewModel.timeLeft.observe(viewLifecycleOwner) { time ->
            if (time < 61) {
                binding.tvLeftTime.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.coral)
                )
                binding.tvLeftTime.text = String.format(requireContext().getString(R.string.title_left_time) + "00:%02d", time)
            }
        }

        binding.cbRecording.setOnCheckedChangeListener { cb, isChecked ->
            val isEnglish = language.startsWith("en")

            if (cb.isChecked) {
//                DWLog.d("답변 하기")
                // 듣기 버튼 상태
                binding.btnPlay.isEnabled = false
                binding.btnPlay.setBackgroundResource(R.drawable.selector_dialog_play_btn) // 혹은 원하는 배경
                binding.tvPlay.setTextColor(Color.GRAY)

                // 저장 버튼 상태
                binding.btnSave.isEnabled = false
                binding.tvSave.setTextColor(Color.DKGRAY)

                // 녹음 텍스트

                binding.tvRecording.text = requireContext().getString(R.string.title_end_answer)
                binding.tvRecording.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.coral)
                )
                viewModel.startTimer()
                viewModel.startRecording()
            } else {
//                DWLog.d("답변 종료")
                // 듣기 버튼 상태 -> 체크되지 않음 && 활성화 상태
                binding.btnPlay.isEnabled = true
                binding.tvPlay.setTextColor(Color.GRAY)

                binding.btnSave.isEnabled = true
                binding.tvSave.setTextColor(Color.GRAY)

                binding.tvRecording.text = requireContext().getString(R.string.title_start_answer)
                binding.tvRecording.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isEnglish) 28f else 32f
                )
                binding.tvRecording.setTextColor(Color.BLACK)

                viewModel.pauseTimer()
                viewModel.stopRecording()
            }
        }

        // 재생 버튼
        binding.btnPlay.setOnClickListener {
            when (viewModel.playStatus) {
                SpeechViewModel.PlayStatus.INIT, SpeechViewModel.PlayStatus.STOP -> {
                    // 재생 시작
//                    DWLog.d("재생 시작")
                    viewModel.playWavFile()
                    updatePlayUI(isPlaying = true)
                }

                SpeechViewModel.PlayStatus.PLAY -> {
                    // 재생 중이면 일시정지
//                    DWLog.d("재생 중이면 일시정지")
                    viewModel.pauseWavFile()
                    updatePlayUI(isPlaying = false)
                }

                SpeechViewModel.PlayStatus.PAUSE -> {
                    // 일시정지 상태면 재생 재개
//                    DWLog.d("일시정지 상태면 재생 재개")
                    viewModel.resumeWavFile()
                    updatePlayUI(isPlaying = true)
                }
            }
        }

        // 저장하기 버튼
        binding.btnSave.setOnClickListener {
            binding.btnSave.isEnabled = false
            viewModel.insertLog()
        }

        binding.ivCancel.setOnClickListener {
            binding.customToolbar.visibility = View.VISIBLE
            binding.layoutQuestionDetail.visibility = View.VISIBLE
            binding.layoutAnswerDetail.visibility = View.VISIBLE
            binding.layoutSelectDetail.visibility = View.VISIBLE
            updateAnswerDisplay()
            binding.layoutRecording.visibility = View.GONE

            viewModel.stopWavFile()
            viewModel.stopRecording()
        }

        viewModel.onPlayCompleted = {
            DWLog.d("재생 완료 콜백 수신")
            updatePlayUI(isPlaying = false)
        }
    }

    private fun initView() {
        DWLog.d("initView")
        // 듣기 버튼 상태
        binding.btnPlay.isEnabled = false
        binding.btnPlay.setBackgroundResource(R.drawable.selector_dialog_play_btn) // 혹은 원하는 배경
        binding.tvPlay.setTextColor(Color.GRAY)

        // 저장 버튼 상태
        binding.btnSave.isEnabled = false
        binding.tvSave.setTextColor(Color.GRAY)

        // 녹음 텍스트
        binding.cbRecording.isChecked = false
        binding.tvRecording.text = requireContext().getString(R.string.title_start_answer)
        binding.tvRecording.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))
    }

    private fun updatePlayUI(isPlaying: Boolean) {
        if (isPlaying) {
            binding.btnPlay.isEnabled = true
            binding.tvPlay.text = getString(R.string.title_stop_answer)
            binding.tvPlay.setTextColor(Color.BLACK)
            binding.btnPlay.setBackgroundResource(R.drawable.btn_play_pause) // pause 아이콘으로 변경

            binding.cbRecording.isEnabled = false
            binding.tvRecording.text = getString(R.string.title_start_answer)
            binding.tvRecording.setTextColor(Color.GRAY)

            binding.btnSave.isEnabled = false
            binding.tvSave.setTextColor(Color.GRAY)
        } else {
            binding.btnPlay.isEnabled = true
            binding.tvPlay.text = getString(R.string.title_listen_answer) // '듣기'
            binding.tvPlay.setTextColor(Color.BLACK)
            binding.btnPlay.setBackgroundResource(R.drawable.selector_dialog_play_btn) // play 아이콘으로 변경

            binding.cbRecording.isEnabled = true
            binding.tvRecording.text = getString(R.string.title_start_answer)
            binding.tvRecording.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))

            binding.btnSave.isEnabled = true
            binding.tvSave.setTextColor(Color.BLACK)
        }
    }

    private fun updateAnswerDisplay() {
        val maxLength = 20
        val event = viewModel.logDtlEvent.value ?: return
        val answers = event.autobiographyMap?.list ?: emptyList()

        if (answers.isNotEmpty() && viewModel.transText.value != "") {
            binding.layoutAnswerDetail.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.new_answer_detail_background)
            binding.tvListenAnswer.background = ContextCompat.getDrawable(activity as MainActivity, R.drawable.btn_listen_checkbox)
            binding.tvAnswer.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_33))

            val currentAnswer = answers[currentAnswerIndex]
            binding.tvAnswer.text = if (currentAnswer.transText.length > maxLength) {
                requireContext().getString(R.string.prefix_title_answer) + currentAnswer.transText.take(maxLength) + "…" // 20자 넘어가면 끝에 … 붙임
            } else {
                requireContext().getString(R.string.prefix_title_answer) + currentAnswer.transText
            }

            binding.tvAnswerStatus.visibility = View.VISIBLE
            val tvSpeech = when (currentAnswerIndex) {
                0 -> requireContext().getString(R.string.text_reading_your_answer_1)
                1 -> requireContext().getString(R.string.text_reading_your_answer_2)
                2 -> requireContext().getString(R.string.text_reading_your_answer_3)
                else -> {
                    ""
                }
            }
            binding.tvSpeech.text = tvSpeech

            if (currentAnswer.transText.length <= maxLength) {
                binding.tvAnswerStatus.text = requireContext().getString(R.string.text_reply_listen) // "다시듣기"
                binding.tvAnswerStatus.setOnClickListener {
                    val audioUrl = viewModel.answerAudioUrl.value
                    if (!audioUrl.isNullOrBlank()) {
                        viewModel.startUrlSpeech(audioUrl)
                    } else {
                        DWLog.e("Audio URL is null or blank")
                        viewModel.startSpeech(currentAnswer.transText)
                    }
                    viewModel.setSpeakingTarget(SpeakingTarget.ANSWER)
                }
            } else {
                binding.tvAnswerStatus.text = requireContext().getString(R.string.text_full_view) // "전체 보기"
                binding.tvAnswerStatus.setOnClickListener {
                    activity?.let { activity ->
                        FullTextDialog(activity)
                            .setTtsText(
                                currentAnswer.transText,
                                { text ->
                                    val audioUrl = currentAnswer.answerAudioUrl
                                    if (!audioUrl.isNullOrBlank()) {
                                        viewModel.startUrlSpeech(audioUrl)
                                    } else {
                                        DWLog.e("Audio URL is null or blank")
                                        viewModel.startSpeech(text)
                                    }
                                },
                                tvSpeech
                            )
                            .setDialogListener(object : FullTextDialog.DialogListener {
                                override fun onCancelTts() {
                                    viewModel.stopSpeech()
                                }
                            })
                            .show()
                    }
                }
            }

            viewModel.setAnswerAudioUrl(currentAnswer.answerAudioUrl ?: "")
            viewModel.setLogId((currentAnswer.autobiographyLogId ?: -1).toString())
            if (answers.size == 1) {
                binding.btnLeft.visibility = View.GONE
                binding.btnRight.visibility = View.GONE
            } else {
                binding.btnLeft.visibility = if (currentAnswerIndex > 0) View.VISIBLE else View.GONE
                binding.btnRight.visibility = if (currentAnswerIndex < answers.size - 1) View.VISIBLE else View.GONE
            }

            // 추가답변 버튼 텍스트 및 상태 설정
            if (answers.size >= 3) {
                binding.tvRetry.text = requireContext().getString(R.string.title_additional_answer) + " (${currentAnswerIndex + 1}/${answers.size})"
                binding.tvRetry.isEnabled = false
                binding.tvRetry.isClickable = false
                binding.tvRetry.alpha = 0.5f // 비활성화된 듯한 UI 표현
            } else if (answers.size > 1) {
                val current = currentAnswerIndex + 1
                val total = answers.size
                binding.tvRetry.text = requireContext().getString(R.string.title_additional_answer) + " ($current/$total)"
                binding.tvRetry.isEnabled = true
                binding.tvRetry.isClickable = true
                binding.tvRetry.alpha = 1f
            } else {
                binding.tvRetry.text = requireContext().getString(R.string.title_additional_answer)
                binding.tvRetry.isEnabled = true
                binding.tvRetry.isClickable = true
                binding.tvRetry.alpha = 1f
            }

        } else {
            // 답변이 없을 때 기본 텍스트
            binding.tvRetry.text = requireContext().getString(R.string.title_additional_answer)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(receiver)
    }
}