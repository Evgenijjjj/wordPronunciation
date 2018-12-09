package evgeny.example.admin.wordpronucation.fragments

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import evgeny.example.admin.wordpronucation.AddNewTopicActivity
import evgeny.example.admin.wordpronucation.R
import evgeny.example.admin.wordpronucation.database.VocabularyDataBase
import evgeny.example.admin.wordpronucation.database.interfaces.TopicDataDao
import evgeny.example.admin.wordpronucation.views.AddNewTopicListRow
import evgeny.example.admin.wordpronucation.views.TopicListRow
import kotlinx.android.synthetic.main.topic_list_fragment.*
import kotlinx.android.synthetic.main.topic_list_row.*


const val TOPICS_FRAGMENT_LOG = "topics_fragment_log"
class TopicListFragment: Fragment() {
    companion object {
        var adapter: GroupAdapter<ViewHolder>? = null
        var recyclerView: RecyclerView? = null
    }

    var callback: (() -> Unit)? = null

    private val requestCodeFR = 111
    private var topicDataDao: TopicDataDao? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.topic_list_fragment, container, false)
    }

    override fun onStart() {
        adapter = GroupAdapter()
        topicDataDao = VocabularyDataBase.getInstance(activity!!)?.TopicDataDao()
        recyclerView = recyclerview_topic_fragment

        adapter?.add(TopicListRow(activity!!, getString(R.string.random_words), true))
        adapter?.add(TopicListRow(activity!!, getString(R.string.elementary_level), true))
        adapter?.add(TopicListRow(activity!!, getString(R.string.middle_level), true))
        adapter?.add(TopicListRow(activity!!, getString(R.string.advanced_level), true))

        for (topicData in topicDataDao?.getAll()!!)
            adapter?.add(TopicListRow(activity!!, topicData.topic, false))

        adapter?.add(AddNewTopicListRow(activity!!.baseContext))

        adapter?.setOnItemClickListener { item, view ->
            try {
                val row = item as AddNewTopicListRow
                startActivityForResult(Intent(activity, AddNewTopicActivity::class.java), requestCodeFR)
                return@setOnItemClickListener
            } catch (e: Exception) {}


            val row = item as TopicListRow

            activity!!.getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)
                .edit()
                .putString(getString(R.string.keyWordKey), row.topicName)
                .apply()

            callback?.invoke()
        }

        recyclerview_topic_fragment.adapter = adapter

        recyclerview_topic_fragment.setOnClickListener {
            if (it == remove_topic_list_row)
                Toast.makeText(activity, "remove !!", Toast.LENGTH_LONG).show()
        }

        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCodeFR && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val topicName = data.getStringExtra(getString(R.string.addNewTopicActivityResult))
                if (topicName == null) {
                    Toast.makeText(activity, "SAVING IN DB ERROR", Toast.LENGTH_LONG).show()
                    return
                }

                Log.d(TOPICS_FRAGMENT_LOG, topicName.toString())

                var pos = adapter?.itemCount!! - 2
                pos = if(pos < 0) 0 else pos

                adapter?.add(pos,TopicListRow(activity!!.baseContext, topicName, true))
                adapter?.notifyDataSetChanged()
            }catch (e: Exception) { }
        }
        else{

        }
    }
}