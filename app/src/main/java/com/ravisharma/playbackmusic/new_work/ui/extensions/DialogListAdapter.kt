package com.ravisharma.playbackmusic.new_work.ui.extensions

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ravisharma.playbackmusic.R

class DialogListAdapter(
    private val items: List<LongItemClick>,
    private val itemClick: (LongItemClick) -> Unit
): BaseAdapter() {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: View.inflate(parent?.context, R.layout.adapter_alert_list_new, null)
        view.findViewById<TextView>(R.id.tvTitle).text = items[position].title
        view.findViewById<ImageView>(R.id.ivIcon).setImageResource(items[position].icon)
        view.setOnClickListener { itemClick(items[position]) }
        return view
    }
}