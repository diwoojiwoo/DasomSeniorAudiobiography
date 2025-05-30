package com.onethefull.dasomautobiography.ui.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.data.model.audiobiography.Item
import com.onethefull.dasomautobiography.databinding.FragmentQuestionlistBinding
import com.onethefull.dasomautobiography.utils.InjectorUtils
import com.onethefull.dasomautobiography.utils.bus.RxBus
import com.onethefull.dasomautobiography.utils.bus.RxEvent
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry

class QuestionListFragment : Fragment(), ListAdapter.OnItemClickListener {
    private lateinit var binding: FragmentQuestionlistBinding
    private lateinit var adapter: ListAdapter
    private lateinit var mItem: Entry

    val viewModel: QuestionListViewModel by viewModels {
        InjectorUtils.provideQuestionListViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuestionlistBinding.inflate(inflater, container, false).apply {}

        // RecyclerView 설정
        adapter = ListAdapter(emptyList(), this) // 빈 리스트로 초기화
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onItemClick(entry: Entry) {
        entry.typeName = mItem.typeName
        (activity as MainActivity).viewModel.selectItem(entry)  // 데이터 저장
        if (entry.answerYn == "N") {
            (activity as MainActivity).navigateToSpeechFragment()  // 답변이 없는 경우 음성입력 화면으로 이동
        } else {
            findNavController().navigate(QuestionListFragmentDirections.actionQuestionlistFragmentToDetailFragment()) // 답변이 있는 경우 질문상세화면으로 이도
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: QuestionListFragmentArgs by navArgs()
        mItem = args.entry

        binding.toolbarTitle.text = mItem.typeName

        binding.btnBack.setOnClickListener {
            (activity as MainActivity).back()
        }

        binding.btnHome.setOnClickListener {
            RxBus.publish(RxEvent.destroyApp)
        }

        // LiveData 관찰
        viewModel.itemList.observe(viewLifecycleOwner) { items ->
            binding.progressBar.visibility = View.GONE
            adapter.updateItems(items) // 데이터 변경 시 어댑터 업데이트
        }

        // 데이터 로딩
        mItem.type.let { item ->
            binding.progressBar.visibility = View.VISIBLE
            viewModel.requestQuestionList(item)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }

    companion object {}
}