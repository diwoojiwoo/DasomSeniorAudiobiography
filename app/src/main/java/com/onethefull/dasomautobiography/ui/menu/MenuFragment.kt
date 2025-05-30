package com.onethefull.dasomautobiography.ui.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.databinding.FragmentMenuBinding
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.MenuItemToEntryMapper
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.wonderfulrobotmodule.scene.SceneHelper

class MenuFragment : Fragment() {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var menuAdapter: MenuAdapter

    val viewModel: MenuViewModel by viewModels {
        InjectorUtils.provideMenuViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE   // ProgressBar 시작 (로딩 중)

        menuAdapter = MenuAdapter(requireContext(), listOf()) // Adapter 초기화 (빈 리스트)
        binding.gvMenu.adapter = menuAdapter

        // ViewModel에서 데이터 가져오기
        viewModel.items.observe(viewLifecycleOwner) { newItems ->
            // 데이터가 로드되면 ProgressBar 숨기기
            binding.progressBar.visibility = View.GONE
            menuAdapter.updateItems(newItems)
        }
        viewModel.getCategoryList()

        // 그리드뷰 아이템 클릭 이벤트
        binding.gvMenu.setOnItemClickListener { _, _, position, _ ->
            val clickedMenuItem = menuAdapter.getItem(position)
            val entry = MenuItemToEntryMapper().map(clickedMenuItem)
            val action =  MenuFragmentDirections.actionMenuFragmentToQuestionlistFragment(entry)
            findNavController().navigate(action)
        }

        // 버튼 이벤트
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack() // 현재 프래그먼트만 제거
        }

        binding.btnHome.setOnClickListener {
            RxBus.publish(RxEvent.destroyApp) // 앱 종료
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}
