package com.onethefull.dasomautobiography

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
import com.onethefull.dasomautobiography.manager.MentManager
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.logger.DWLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding
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
        val actionName = arguments?.getString(OnethefullBase.PARAM_ACTION_NAME)

        MentManager.clear()
        actionName?.let { MentManager.currentActionName = it }
        DWLog.d("SplashFragment initialized: actionName=$actionName")

        // --- 인트로 멘트 가져오기 ---
        viewModel.getCategoryList()

        // --- 발화 끝나면 getContent() 실행 ---
        viewModel.isSpeechFinished.observe(viewLifecycleOwner) { isSpeechFinished ->
            if(isSpeechFinished) {
                if (actionName == OnethefullBase.ACTION_COMMAND) {
                    viewModel.getContent()
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(1500)
                        findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
                    }
                }
            }
        }

        // --- getContent 완료 후 화면 이동 ---
        viewModel.contentLoaded.observe(viewLifecycleOwner) { loaded ->
            if (loaded) {
                viewModel.resetContentLoaded()
                viewLifecycleOwner.lifecycleScope.launch {
                    (activity as? MainActivity)?.navigateToSpeechFragment(Constant.MOVE_REASON_STT)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}
