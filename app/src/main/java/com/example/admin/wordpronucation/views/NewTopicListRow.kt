package com.example.admin.wordpronucation.views

import android.text.Editable
import android.text.TextWatcher
import com.example.admin.wordpronucation.R
import com.example.admin.wordpronucation.models.WordPair
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.new_topic_row.view.*

class NewTopicListRow(private val pair: WordPair): Item<ViewHolder>() {
    var enlishWord: String? = null
    var transWord: String? = null

    override fun getLayout(): Int {
        return R.layout.new_topic_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.original_word_new_topic_row.text = pair.originalWord
        viewHolder.itemView.translated_word_new_topic_row.setText(pair.translatedWord)

        this.enlishWord = pair.originalWord
        this.transWord = pair.translatedWord

        viewHolder.itemView.translated_word_new_topic_row.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {transWord = p0.toString()}
        })

    }
}