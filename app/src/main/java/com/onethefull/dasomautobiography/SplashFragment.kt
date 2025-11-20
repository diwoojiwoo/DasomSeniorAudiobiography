package com.onethefull.dasomautobiography

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.data.model.audiobiography.Ment
import com.onethefull.dasomautobiography.databinding.FragmentSplashBinding
import com.onethefull.dasomautobiography.manager.MentManager
import com.onethefull.dasomautobiography.utils.Constant
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.logger.DWLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 2. 12.
 */
class SplashFragment : Fragment() {
    private lateinit var binding : FragmentSplashBinding
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
        val nextAction = arguments?.getString(OnethefullBase.PARAM_NEXT_SCENE_ACTION)
        DWLog.d("actionName = ${actionName}, nextAction = $nextAction")

        viewModel.getCategoryList(actionName.toString())
        viewModel.contentLoaded.observe(viewLifecycleOwner) { loaded ->
            if (!loaded) return@observe

            val hasStartMent = when(actionName) {
                OnethefullBase.ACTION_SMARTFRIEND -> !MentManager.smartfriendMent?.start.isNullOrEmpty()
                OnethefullBase.ACTION_COMMAND -> !MentManager.commandMent?.start.isNullOrEmpty()
                else -> false
            }

            if (hasStartMent) {
                val observer = object : Observer<Boolean> {
                    override fun onChanged(finished: Boolean) {
                        if (finished) {
                            viewModel.isSpeechFinished.removeObserver(this)
                            processNext(nextAction)
                        }
                    }
                }
                viewModel.isSpeechFinished.observe(viewLifecycleOwner, observer)
            } else {
                processNext(nextAction)
            }
        }
    }

    private fun processNext(nextAction: String?) {
        if (!nextAction.isNullOrEmpty()) {
            viewModel.getContent()
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1500)
                findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}
