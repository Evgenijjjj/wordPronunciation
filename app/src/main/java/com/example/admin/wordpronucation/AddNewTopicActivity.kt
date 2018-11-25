package com.example.admin.wordpronucation

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.admin.wordpronucation.database.VocabularyDataBase
import com.example.admin.wordpronucation.database.helpers.WordsPairDataBaseHelper
import com.example.admin.wordpronucation.database.interfaces.TopicDataDao
import com.example.admin.wordpronucation.database.interfaces.WordsPairDataDao
import com.example.admin.wordpronucation.database.tables.TopicData
import com.example.admin.wordpronucation.database.tables.WordsPairData
import com.example.admin.wordpronucation.models.WordPair
import com.example.admin.wordpronucation.retrofit.MyApi
import com.example.admin.wordpronucation.retrofit.RestHelper
import com.example.admin.wordpronucation.retrofit.TranslatorRetrofitClient
import com.example.admin.wordpronucation.retrofit.WordRetrofitClient
import com.example.admin.wordpronucation.views.NewTopicListRow
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_new_topic.*

const val ADD_NEW_TOPIC_ACTIVITY_TAG = "add_new_topic_activity"
class AddNewTopicActivity : Activity() {
    private var checkFlag = false
    private var adapter: GroupAdapter<ViewHolder>? = null

    private var topicDataDao: TopicDataDao? = null
    private var wordsPairDataDao: WordsPairDataDao? = null
    private var wordsPairDataBaseHelper: WordsPairDataBaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_topic)

        topicDataDao = VocabularyDataBase.getInstance(this)?.TopicDataDao()
        wordsPairDataDao = VocabularyDataBase.getInstance(this)?.WordsPairDataDao()
        wordsPairDataBaseHelper = WordsPairDataBaseHelper(this)

        push_btn_add_topic_activity.setOnClickListener {
            if (!checkFlag) {
                if (edit_text_add_topic_activity.text.isEmpty()) {
                    Toast.makeText(this, "Field is Empty!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                push_btn_add_topic_activity.text = getString(R.string.save)
                findWordsPairs()
                checkFlag = true
                return@setOnClickListener
            }

            Toast.makeText(this, "SAVING...", Toast.LENGTH_LONG).show()

            var topicData = TopicData()
            topicData.topic = edit_text_add_topic_activity.text.toString()
            topicDataDao?.insert(topicData)
            topicData = topicDataDao?.getLastTopic()!!


            for (i in 0..(adapter!!.itemCount - 1)) {
                val row = adapter?.getItem(0) as NewTopicListRow

                var wordsPairData = WordsPairData()
                wordsPairData.engWord = row.enlishWord!!
                wordsPairData.translatedWord = row.transWord!!
                wordsPairDataDao?.insert(wordsPairData)
                wordsPairData = wordsPairDataDao?.getLastWordsPair()!!

                wordsPairDataBaseHelper?.addWordsPairInDB(wordsPairData, topicData)
            }

            val i = Intent()
            i.putExtra(getString(R.string.addNewTopicActivityResult), topicData.topic)
            setResult(Activity.RESULT_OK, i)
            finish()
        }

        edit_text_add_topic_activity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                push_btn_add_topic_activity.text = getString(R.string.check)
                checkFlag = false
            }
        })
    }

    private fun findWordsPairs() {
        val jsonTranslatorApi = TranslatorRetrofitClient.instance.create(MyApi::class.java)
        val jsonWordsApi = WordRetrofitClient.instance.create(MyApi::class.java)
        adapter = GroupAdapter()

        recyclerview_new_words_activity_add_new_topic.adapter = adapter

        CompositeDisposable().add(jsonTranslatorApi.getTranslation("translate?key=" + getString(R.string.translatorApiKey) +
                "&text=" + edit_text_add_topic_activity.text.toString() + "&lang=" + "ru-en")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->

                CompositeDisposable().add(jsonWordsApi.getWordsForTheme(getString(R.string.getWordsFromPhraseUrl) + result.text[0])
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { wordsList ->
                            for (enWord in wordsList) {
                                if (!enWord.word.contains("[0-9]") && !enWord.word.contains(' '))

                                    CompositeDisposable().add(jsonTranslatorApi.getTranslation("translate?key=" + getString(R.string.translatorApiKey) +
                                            "&text=" + enWord.word + "&lang=" + "en-ru")
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe { translatedWord ->
                                            adapter?.add(NewTopicListRow(WordPair(enWord.word, translatedWord.text[0])))
                                        })

                            }
                        }
                )


            })
    }

}
