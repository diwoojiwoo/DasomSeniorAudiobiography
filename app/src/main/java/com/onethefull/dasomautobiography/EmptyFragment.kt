package com.onethefull.dasomautobiography

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.databinding.FragmentEmptyBinding
import com.onethefull.dasomautobiography.utils.logger.DWLog

class EmptyFragment : Fragment() {

    private var _binding: FragmentEmptyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmptyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("EmptyFragment onViewCreated")

        // 앱 시작 시 바로 SplashFragment로 이동
        val actionName = activity?.intent?.getStringExtra(OnethefullBase.PARAM_ACTION_NAME)
        val nextAction = activity?.intent?.getStringExtra(OnethefullBase.PARAM_NEXT_SCENE_ACTION)

        val bundle = Bundle().apply {
            putString(OnethefullBase.PARAM_ACTION_NAME, actionName)
            putString(OnethefullBase.PARAM_NEXT_SCENE_ACTION, nextAction)
        }

        findNavController().navigate(R.id.splash_fragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
