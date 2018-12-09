package evgeny.example.admin.wordpronucation

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import evgeny.example.admin.wordpronucation.database.VocabularyDataBase
import evgeny.example.admin.wordpronucation.database.helpers.WordsPairDataBaseHelper
import evgeny.example.admin.wordpronucation.database.interfaces.TopicDataDao
import evgeny.example.admin.wordpronucation.database.interfaces.WordsPairDataDao
import evgeny.example.admin.wordpronucation.database.tables.TopicData
import evgeny.example.admin.wordpronucation.database.tables.WordsPairData
import evgeny.example.admin.wordpronucation.models.WordPair
import evgeny.example.admin.wordpronucation.retrofit.MyApi
import evgeny.example.admin.wordpronucation.retrofit.TranslatorRetrofitClient
import evgeny.example.admin.wordpronucation.retrofit.WordRetrofitClient
import evgeny.example.admin.wordpronucation.views.NewTopicListRow
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_new_topic.*

const val ADD_NEW_TOPIC_ACTIVITY_TAG = "add_new_topic_activity"
class AddNewTopicActivity : Activity() {
    companion object {
        var adapter: GroupAdapter<ViewHolder>? = null
        var recyclerView: RecyclerView? = null
    }
    private var checkFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_topic)

        recyclerView = recyclerview_new_words_activity_add_new_topic

        push_btn_add_topic_activity.setOnClickListener {
            if (!checkFlag) {
                if (edit_text_add_topic_activity.text.isEmpty()) {
                    Toast.makeText(this, "Field is Empty!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                push_btn_add_topic_activity.visibility = View.INVISIBLE
                push_btn_add_topic_activity.text = getString(R.string.save)
                findWordsPairs()
                checkFlag = true
                yandex_ref_add_new_topic.visibility = View.VISIBLE
                return@setOnClickListener
            }

            Toast.makeText(this, "SAVING...", Toast.LENGTH_LONG).show()
            savingInDbTask().execute()
        }

        edit_text_add_topic_activity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                push_btn_add_topic_activity.text = getString(R.string.check)
                checkFlag = false

                if (!p0.isNullOrEmpty() && p0?.last() == '\n'){
                    push_btn_add_topic_activity.performClick()
                    edit_text_add_topic_activity.setText(p0.substring(0, p0.lastIndex))
                    edit_text_add_topic_activity.clearFocus()
                }
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

                CompositeDisposable().add(jsonWordsApi.getWordsForTheme(getString(R.string.getWordsFromPhraseUrl) + result.text[0] + "&max=50")
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
                                            if (!translatedWord.text[0].matches("[a-zA-Z0-9_\\-]+".toRegex())) {
                                                Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "matches: ${translatedWord.text[0]}")
                                                adapter?.add(NewTopicListRow(WordPair(enWord.word, translatedWord.text[0])))
                                             }
                                            push_btn_add_topic_activity.visibility = View.VISIBLE
                                        })

                            }
                        }
                )


            })
    }

    private inner class savingInDbTask: AsyncTask<Int,Int,Void>() {
        private var topicDataDao: TopicDataDao? = null
        private var wordsPairDataDao: WordsPairDataDao? = null
        private var wordsPairDataBaseHelper: WordsPairDataBaseHelper? = null
        private var topicData = TopicData()

        private var progress = 0

        override fun onProgressUpdate(vararg values: Int?) {
            circle_progress_add_new_topic_activity.progress = values[0]!!
        }

        override fun onPostExecute(result: Void?) {
            val i = Intent()
            i.putExtra(getString(R.string.addNewTopicActivityResult), topicData.topic)
            setResult(Activity.RESULT_OK, i)
            finish()
        }

        override fun doInBackground(vararg p0: Int?): Void? {
            for (i in 0..(adapter!!.itemCount - 1)) {
                publishProgress(i)
                try {
                    val row = adapter?.getItem(i) as NewTopicListRow

                    var wordsPairData = WordsPairData()
                    wordsPairData.engWord = row.enlishWord!!
                    wordsPairData.translatedWord = row.transWord!!
                    wordsPairDataDao?.insert(wordsPairData)
                    wordsPairData = wordsPairDataDao?.getLastWordsPair()!!

                    wordsPairDataBaseHelper?.addWordsPairInDB(wordsPairData, topicData)
                    Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "creating: ${wordsPairData.engWord} - ${wordsPairData.translatedWord}")
                    circle_progress_add_new_topic_activity.progress = progress++
                } catch (e: Exception) {}
            }
            return null
        }

        override fun onCancelled(result: Void?) {
            super.onCancelled(result)
        }

        override fun onCancelled() {
            super.onCancelled()
        }

        override fun onPreExecute() {
            super.onPreExecute()

            topicDataDao = VocabularyDataBase.getInstance(this@AddNewTopicActivity)?.TopicDataDao()
            wordsPairDataDao = VocabularyDataBase.getInstance(this@AddNewTopicActivity)?.WordsPairDataDao()
            wordsPairDataBaseHelper = WordsPairDataBaseHelper(this@AddNewTopicActivity)

            topicData.topic = edit_text_add_topic_activity.text.toString()
            topicDataDao?.insert(topicData)
            topicData = topicDataDao?.getLastTopic()!!

            push_btn_add_topic_activity.visibility = View.INVISIBLE
            circle_progress_add_new_topic_activity.visibility = View.VISIBLE
            circle_progress_add_new_topic_activity.max = adapter?.itemCount!! - 1
        }
    }

}
