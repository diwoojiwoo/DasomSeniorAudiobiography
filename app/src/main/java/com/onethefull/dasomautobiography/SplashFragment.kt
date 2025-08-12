package com.onethefull.dasomautobiography

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.databinding.FragmentSplashBinding
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.logger.DWLog
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 2. 12.
 */
class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private var nextAction: String = ""
    private var motionDetected: Boolean = false // 사람 인식을 통해 실행됐는지 여부
    private var effectOn: Boolean = false  // 효과음 재생할건지 여부

    val viewModel: SplashViewModel by viewModels {
        InjectorUtils.provideSplashViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false).apply {}
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("SplashFragment onViewCreated")
        arguments?.let {
            nextAction = it.getString(OnethefullBase.PARAM_NEXT_SCENE_ACTION, "")
            motionDetected = it.getBoolean(OnethefullBase.PARAM_MOTION_DETECTED, false)
            effectOn = it.getBoolean(OnethefullBase.PARAM_EFFECT_ON, false)

            viewModel.setMotionDetected(motionDetected)
            viewModel.setEffectOn(effectOn)
        }

        DWLog.d("nextAction = $nextAction, motionDetected=$motionDetected, effectOn= $effectOn ")
        if (!nextAction.isNullOrEmpty()) {
            // 1. "자서전 실행" 음성 명령 으로 실행 => 인트로 재생 후 자서전 문제 발화
            DWLog.d(" 1. 자서전 실행 음성 명령 으로 실행 => 인트로 재생 후 자서전 문제 발화")
            viewModel.getContent()
        } else {
            if (motionDetected) {
                // 2.  상황인식-자서전
                DWLog.e("2.  상황인식-자서전")
                if (effectOn) {
                    val mediaPlayer = MediaPlayer.create(App.instance, R.raw.effect)
                    mediaPlayer.setOnCompletionListener { it.release() }
                    MainScope().launch {
                        delay(2000)
                        mediaPlayer.start()
                    }
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.startMotionIntro()
                }
            } else {
                // 3.  메뉴 > 자서전 선택 > 메뉴 화면 이동
//                DWLog.e("3.  메뉴 > 자서전 선택 > 메뉴 화면 이동")
//                viewLifecycleOwner.lifecycleScope.launch {
//                    delay(1500)
//                    findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
//                }
                /********Test ******/
                DWLog.e("Test!!!!!!!!!!!!!!!!!!")
                viewLifecycleOwner.lifecycleScope.launch {
                    (activity as? MainActivity)?.navigateToNewSpeechFragment(Constant.MOVE_REASON_STT, motionDetected = motionDetected)
                }
            }
        }

        var hasNavigated = false
        viewModel.speechType.observe(viewLifecycleOwner) { event ->
            if (event == SpeechType.CONTENT && !hasNavigated) {
                hasNavigated = true
                viewLifecycleOwner.lifecycleScope.launch {
                    (activity as? MainActivity)?.navigateToSpeechFragment(Constant.MOVE_REASON_STT, motionDetected = motionDetected)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}
