package evgeny.example.admin.wordpronucation.views

import android.app.AlertDialog
import android.content.Context
import android.view.View
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import evgeny.example.admin.wordpronucation.R
import evgeny.example.admin.wordpronucation.database.VocabularyDataBase
import evgeny.example.admin.wordpronucation.fragments.TopicListFragment
import kotlinx.android.synthetic.main.topic_list_row.view.*

class TopicListRow(private val ctx: Context, private val topic: String,private  val vocabularyFlag: Boolean): Item<ViewHolder>() {
    var topicName: String? = null

    override fun getLayout(): Int {
        return R.layout.topic_list_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.topic_textview_topic_list_row.text = topic
        topicName = topic

        if (!vocabularyFlag) {
            viewHolder.itemView.remove_topic_list_row.visibility = View.VISIBLE
            viewHolder.itemView.web_image_list_row.visibility = View.VISIBLE
        }
        else {
            viewHolder.itemView.vocabulary_image_list_row.visibility = View.VISIBLE
        }

        viewHolder.itemView.remove_topic_list_row.setOnClickListener {
            AlertDialog.Builder(ctx)
                .setMessage("Remove ?")
                .setPositiveButton("Yes") { _, _ ->
                    val position = TopicListFragment.adapter?.getAdapterPosition(this)
                    TopicListFragment.adapter?.remove(this)
                    TopicListFragment.adapter?.notifyItemRemoved(position!!)
                    VocabularyDataBase.getInstance(ctx)?.TopicDataDao()?.deleteTopicWithName(topic)
                    TopicListFragment.recyclerView?.adapter = TopicListFragment.adapter
                }
                .setNegativeButton("Nope") { _, _ ->}
                .show()
        }

    }
}