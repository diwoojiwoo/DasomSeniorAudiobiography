package com.onethefull.dasomautobiography

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.logger.DWLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 2. 12.
 */
class SplashFragment : Fragment() {

    val viewModel : SplashViewModel by viewModels {
        InjectorUtils.provideSplashViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DWLog.d("SplashFragment onCreateView")
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("SplashFragment onViewCreated")
        val nextAction = arguments?.getString(OnethefullBase.PARAM_NEXT_SCENE_ACTION, "")

        viewModel.getContent()
        viewModel.isSpeechFinished.observe(viewLifecycleOwner) { event ->
            if(event) {
                viewLifecycleOwner.lifecycleScope.launch {
//                    if (nextAction != "")
//                        findNavController().navigate(R.id.action_splashFragment_to_speechFragment)
//                    else
//                        findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
                    findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}
