package evgeny.example.admin.wordpronucation.views

import android.text.Editable
import android.text.TextWatcher
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import evgeny.example.admin.wordpronucation.AddNewTopicActivity
import evgeny.example.admin.wordpronucation.R
import evgeny.example.admin.wordpronucation.models.WordPair
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

        viewHolder.itemView.remove_add_new_topic_row.setOnClickListener {
            val position = AddNewTopicActivity.adapter?.getAdapterPosition(this)
            AddNewTopicActivity.adapter?.remove(this)
            AddNewTopicActivity.adapter?.notifyItemRemoved(position!!)
            AddNewTopicActivity.recyclerView?.adapter = AddNewTopicActivity.adapter

            try {
                when(position) {
                    0 -> AddNewTopicActivity.recyclerView?.scrollToPosition(0)
                    AddNewTopicActivity.recyclerView?.adapter?.itemCount ->
                        AddNewTopicActivity.recyclerView?.scrollToPosition(AddNewTopicActivity.recyclerView?.adapter?.itemCount!! - 1)
                    else -> AddNewTopicActivity.recyclerView?.scrollToPosition(position!!)
                }
            } catch (e: Exception) {}
        }
    }
}