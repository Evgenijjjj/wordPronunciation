package com.example.admin.wordpronucation.views

import android.content.Context
import android.widget.Toast
import com.example.admin.wordpronucation.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.topic_list_row.view.*

class TopicListRow(private val ctx: Context, private val topic: String): Item<ViewHolder>() {
    var topic_name: String? = null

    override fun getLayout(): Int {
        return R.layout.topic_list_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.topic_textview_topic_list_row.text = topic

        topic_name = topic
        viewHolder.itemView.download_topic_list_row.setOnClickListener {
            Toast.makeText(ctx, "DOWNLOADING...", Toast.LENGTH_LONG).show()
        }
    }
}