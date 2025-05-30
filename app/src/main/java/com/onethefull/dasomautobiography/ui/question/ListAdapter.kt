package com.onethefull.dasomautobiography.ui.question

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onethefull.dasomautobiography.R
import com.onethefull.dasomautobiography.data.model.audiobiography.Entry
import com.onethefull.dasomautobiography.data.model.audiobiography.Item

/**
 * Created by sjw on 2025. 2. 10.
 */
class ListAdapter(
    private var itemList: List<Entry>,
    private val listener : OnItemClickListener
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumber: TextView = itemView.findViewById(R.id.tv_number)
        private val tvQuestion: TextView = itemView.findViewById(R.id.tv_question)
        private val tvAnswerYN: TextView = itemView.findViewById(R.id.tv_answer_yn)

        fun bind(entry: Entry) {
            tvNumber.text = (position + 1).toString() // 1부터 시작
            tvQuestion.text = entry.viewQuestion

            if (entry.answerYn == "Y")
                tvAnswerYN.setBackgroundResource(R.drawable.icon_complete)
            else
                tvAnswerYN.setBackgroundResource(R.drawable.icon_incomplete)

            itemView.setOnClickListener {
                listener.onItemClick(entry)  // 클릭 이벤트 전달
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    fun updateItems(newItems: List<Entry>) {
        itemList = newItems
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(entry: Entry)
    }
}
