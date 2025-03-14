package com.onethefull.dasomautobiography

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.base.OnethefullBase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2025. 2. 12.
 */
class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextAction = arguments?.getString(OnethefullBase.PARAM_NEXT_SCENE_ACTION, "")

        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) // 2초 후 이동
            if (nextAction != "")
                findNavController().navigate(R.id.action_splashFragment_to_speechFragment)
            else
                findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
        }
    }
}
