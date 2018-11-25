package com.example.admin.wordpronucation.fragments

import android.app.Activity
import android.os.Bundle
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.admin.wordpronucation.AddNewTopicActivity
import com.example.admin.wordpronucation.R
import com.example.admin.wordpronucation.database.VocabularyDataBase
import com.example.admin.wordpronucation.database.interfaces.TopicDataDao
import com.example.admin.wordpronucation.views.AddNewTopicListRow
import com.example.admin.wordpronucation.views.TopicListRow
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.topic_list_fragment.*


const val TOPICS_FRAGMENT_LOG = "topics_fragment_log"
class TopicListFragment: Fragment() {
    private val requestCodeFR = 111
    private var adapter: GroupAdapter<ViewHolder>? = null
    private var topicDataDao: TopicDataDao? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.topic_list_fragment, container, false)
    }

    override fun onStart() {
        adapter = GroupAdapter()
        topicDataDao = VocabularyDataBase.getInstance(activity)?.TopicDataDao()

        adapter?.add(TopicListRow(activity, getString(R.string.random_words)))
        adapter?.add(TopicListRow(activity, getString(R.string.elementary_level)))
        adapter?.add(TopicListRow(activity, getString(R.string.middle_level)))
        adapter?.add(TopicListRow(activity, getString(R.string.advanced_level)))

        for (topicData in topicDataDao?.getAll()!!)
            adapter?.add(TopicListRow(activity, topicData.topic))

        adapter?.add(AddNewTopicListRow(activity))

        adapter?.setOnItemClickListener { item, view ->
            try {
                val row = item as AddNewTopicListRow
                Toast.makeText(activity, "Add new topic...", Toast.LENGTH_LONG).show()
                startActivityForResult(Intent(activity, AddNewTopicActivity::class.java), requestCodeFR)
                return@setOnItemClickListener
            } catch (e: Exception) {}


            val row = item as TopicListRow

            Toast.makeText(activity, "Selected ${row.topic_name}", Toast.LENGTH_LONG).show()

            activity.getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)
                .edit()
                .putString(getString(R.string.keyWordKey), row.topic_name)
                .apply()
            /*val editor = sp.edit()
            editor.putString(getString(R.string.keyWordKey), row.topic_name)
            editor.apply()*/

            finishFragment()
        }

        recyclerview_topic_fragment.adapter = adapter
        super.onStart()
    }

    private fun finishFragment() {
        val ft = fragmentManager.beginTransaction()
        ft?.replace(R.id.recyclerview_fragment_startpage, android.app.Fragment())
        ft?.remove(this)
        ft?.commit()

        activity.recreate()
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

                adapter?.add(pos,TopicListRow(activity, topicName))
                adapter?.notifyDataSetChanged()
            }catch (e: Exception) { }
        }
        else{

        }
    }
}