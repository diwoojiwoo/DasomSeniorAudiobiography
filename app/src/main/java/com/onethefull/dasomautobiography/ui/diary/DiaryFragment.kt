package com.onethefull.dasomautobiography.ui.diary

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.databinding.FragmentDiaryBinding
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.SpeechStatus

/**
 * Created by sjw on 2025. 1. 9.
 */
class DiaryFragment : Fragment() {
    private lateinit var binding: FragmentDiaryBinding

    val viewModel: DiaryViewModel by viewModels {
        InjectorUtils.provideDiaryViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDiaryBinding.inflate(inflater, container, false).apply {
//           viewModel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpIntro()
        setUpDiaryContents()
        setUpSpeech()

        binding.tvContent.movementMethod = ScrollingMovementMethod()
        binding.btnClose.setOnClickListener {
            RxBus.publish(RxEvent.destroyApp)
        }

    }

    private fun setUpIntro() {
        viewModel.intro()
    }

    private fun setUpDiaryContents() {
        viewModel.getDiarySentence()

        viewModel.diaryStatus.observe(viewLifecycleOwner) {
            when (it) {
                DiaryStatus.INTRO -> {
//                    binding.clBg.setBackgroundResource(R.drawable.img_intro_diary)
                    binding.flNotExistDiary.visibility = View.GONE
                    binding.flDiary.visibility = View.GONE
                }

                DiaryStatus.START -> {
                    binding.clBg.setBackgroundResource(R.color.background_color)
                    binding.flDiary.visibility = View.VISIBLE
                    binding.flNotExistDiary.visibility = View.GONE
                }

                DiaryStatus.NOT_EXIST_1,DiaryStatus.NOT_EXIST_2, DiaryStatus.NOT_EXIST_3  -> {
                    binding.clBg.setBackgroundResource(R.color.background_color)
                    binding.flNotExistDiary.visibility = View.VISIBLE
                    binding.flDiary.visibility = View.GONE

                    binding.tvTitle.text = viewModel.data.value!!.title
                    binding.tvDetail.text = viewModel.data.value!!.detail
                    binding.ivNo.setBackgroundResource(viewModel.data.value!!.image)
                }
            }
        }

        viewModel.date.observe(viewLifecycleOwner) {
            if (viewModel.date.value != "") {
                binding.tvDate.text = it.toString()
            }
        }

        viewModel.content.observe(viewLifecycleOwner) {
            if (viewModel.content.value != "") {
                binding.tvContent.text = it.toString()
            }
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
            }

            SpeechStatus.SPEECH -> {
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }

}