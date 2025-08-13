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
import com.onethefull.dasomautobiography.contents.toast.Toasty
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.utils.Constant

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
        if (entry.answerYn == Constant.NO) {
            (activity as MainActivity).navigateToNewSpeechFragment(Constant.MOVE_REASON_NO_ANSWER)  // 답변이 없는 경우 SpeechFragment 화면으로 이동
        } else {
            findNavController().navigate(QuestionListFragmentDirections.actionQuestionlistFragmentToDetailFragment()) // 답변이 있는 경우 QuestionDetailFragment 이동
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

        viewModel.itemList.observe(viewLifecycleOwner) { items ->
            binding.progressBar.visibility = View.GONE
            adapter.updateItems(items)
        }

        viewModel.questionListEvent.observe(viewLifecycleOwner) { event ->
            binding.progressBar.visibility = View.GONE
            when (event.status_code) {
                1001, -3 -> {
                    Toasty.error(requireContext(), event.status.toString()).show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }

                -1 -> {
                    Toasty.error(requireContext(), event.status ?: getString(R.string.message_network_error)).show()
                    RxBus.publish(RxEvent.destroyShortAppUpdate)
                }

                else -> {

                }
            }
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