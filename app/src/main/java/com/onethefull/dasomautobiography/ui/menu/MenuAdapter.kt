package com.onethefull.dasomautobiography.ui.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.onethefull.dasomautobiography.R
import androidx.core.content.ContextCompat
import com.onethefull.dasomautobiography.utils.logger.DWLog

/**
 * Created by sjw on 2025. 2. 10.
 */
class MenuAdapter(private val context: Context, private var items: List<MenuItem2>) : BaseAdapter() {

    fun updateItems(newItems: List<MenuItem2>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): MenuItem2 = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false)
        val ivMenu = view.findViewById<ImageView>(R.id.iv_menu)
        val tvMenu = view.findViewById<TextView>(R.id.tv_menu)
        val tvTotalCnt = view.findViewById<TextView>(R.id.tv_total_cnt)


        val item = items[position]
        tvMenu.text = item.typeName
        tvTotalCnt.text = "${item.answerCnt}/${item.questionCnt}"
        Glide.with(context)
            .load(item.imgUrl)
            .placeholder(ContextCompat.getDrawable(context, android.R.drawable.ic_popup_sync))
            .error(ContextCompat.getDrawable(context, R.drawable.item))
            .into(ivMenu)
        return view
    }
}
