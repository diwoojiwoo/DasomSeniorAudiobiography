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
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.Status
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

    val desiredOrder = listOf("init", "parents", "childhood", "school", "youthhood")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        DWLog.d("MenuFragment onCreateView")
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("MenuFragment onViewCreated")
        binding.progressBar.visibility = View.VISIBLE   // ProgressBar 시작 (로딩 중)

        menuAdapter = MenuAdapter(requireContext(), listOf()) // Adapter 초기화 (빈 리스트)
        binding.gvMenu.adapter = menuAdapter
        viewModel.items.observe(viewLifecycleOwner) { newItems ->
            binding.progressBar.visibility = View.GONE
            val sortedItems = newItems.sortedBy { item ->
                val index = desiredOrder.indexOf(item.type)
                if (index == -1) Int.MAX_VALUE else index
            }
            menuAdapter.updateItems(sortedItems)
        }
        viewModel.getCategoryList()
        viewModel.categoryStatusEvent.observe(viewLifecycleOwner) { event ->
            when (event.status_code) {
                1001, -3 -> {
                    Toasty.error(requireContext(), event.status ?: "status is null, code :: ${event.status_code}").show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }

                -1 -> {
                    Toasty.error(requireContext(), event.status ?: getString(R.string.message_network_error)).show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }

                0 -> {}

                else -> {
                    Toasty.error(requireContext(), ("status :: " + event.status + ", status code :: " + event.status_code) ?: getString(R.string.message_network_error)).show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }
            }
        }
        binding.gvMenu.setOnItemClickListener { _, _, position, _ ->
            val clickedMenuItem = menuAdapter.getItem(position)
            val entry = MenuItemToEntryMapper().map(clickedMenuItem)
            val action = MenuFragmentDirections.actionMenuFragmentToQuestionlistFragment(entry)
            findNavController().navigate(action)
        }

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
