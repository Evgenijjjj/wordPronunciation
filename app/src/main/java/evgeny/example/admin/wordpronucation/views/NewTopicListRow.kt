package evgeny.example.admin.wordpronucation.views


import android.util.Log
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import evgeny.example.admin.wordpronucation.ADD_NEW_TOPIC_ACTIVITY_TAG
import evgeny.example.admin.wordpronucation.AddNewTopicActivity
import evgeny.example.admin.wordpronucation.AddNewTopicActivity.Companion.wordsPairsList
import evgeny.example.admin.wordpronucation.R
import evgeny.example.admin.wordpronucation.models.WordPair
import kotlinx.android.synthetic.main.new_topic_row.view.*


class NewTopicListRow(private val pair: WordPair): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.new_topic_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.original_word_new_topic_row.text = pair.originalWord
        viewHolder.itemView.translated_word_new_topic_row.setText(pair.translatedWord)

        viewHolder.itemView.remove_add_new_topic_row.setOnClickListener {
            try {
                AddNewTopicActivity.adapter?.remove(this)
                wordsPairsList.remove(pair)
                Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "REMOVE PAIR: ${pair.originalWord}, ${pair.translatedWord}")
            } catch (e: java.lang.IllegalArgumentException) {
                Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "row ex: " + e.toString())
            }
        }
    }
}