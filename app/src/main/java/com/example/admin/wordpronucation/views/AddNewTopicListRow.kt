package com.example.admin.wordpronucation.views

import android.content.Context
import com.example.admin.wordpronucation.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class AddNewTopicListRow (private val ctx: Context): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.add_new_topic_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }
}