package evgeny.example.admin.wordpronucation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import evgeny.example.admin.wordpronucation.database.VocabularyDataBase
import evgeny.example.admin.wordpronucation.database.helpers.WordsPairDataBaseHelper
import evgeny.example.admin.wordpronucation.database.interfaces.TopicDataDao
import evgeny.example.admin.wordpronucation.database.interfaces.WordsPairDataDao
import evgeny.example.admin.wordpronucation.database.tables.TopicData
import evgeny.example.admin.wordpronucation.database.tables.WordsPairData
import evgeny.example.admin.wordpronucation.models.Word
import evgeny.example.admin.wordpronucation.models.WordPair
import evgeny.example.admin.wordpronucation.parsing.PARSING_LOG
import evgeny.example.admin.wordpronucation.parsing.Query
import evgeny.example.admin.wordpronucation.retrofit.MyApi
import evgeny.example.admin.wordpronucation.retrofit.WordRetrofitClient
import evgeny.example.admin.wordpronucation.views.NewTopicListRow
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_new_topic.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder

const val ADD_NEW_TOPIC_ACTIVITY_TAG = "add_new_topic_activity"

class AddNewTopicActivity : Activity() {
    companion object {
        var adapter: GroupAdapter<ViewHolder> = GroupAdapter()
        val wordsPairsList = ArrayList<WordPair>()
    }

    private var checkFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_topic)
        recyclerview_new_words_activity_add_new_topic.adapter = adapter

        push_btn_add_topic_activity.setOnClickListener {
            if (!checkFlag) {
                if (edit_text_add_topic_activity.text.isEmpty()) {
                    Toast.makeText(this, "Field is Empty!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                push_btn_add_topic_activity.visibility = View.INVISIBLE
                push_btn_add_topic_activity.text = getString(R.string.save)

                wordsPairsList.clear()
                val task = SearchWordsTask(edit_text_add_topic_activity.text.toString())
                task.execute()
                //findWords(edit_text_add_topic_activity.text.toString())
                checkFlag = true

                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(edit_text_add_topic_activity.applicationWindowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                edit_text_add_topic_activity.clearFocus()

                return@setOnClickListener
            }

            if (adapter.itemCount == 0) {
                push_btn_add_topic_activity.text = getString(R.string.check)
                Toast.makeText(this, "List is empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "SAVING...", Toast.LENGTH_LONG).show()
            SavingInDbTask().execute()
        }

        edit_text_add_topic_activity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                push_btn_add_topic_activity.text = getString(R.string.check)
                checkFlag = false

                if (!p0.isNullOrEmpty() && p0?.last() == '\n') {
                    push_btn_add_topic_activity.performClick()
                    edit_text_add_topic_activity.setText(p0.substring(0, p0.lastIndex))
                    edit_text_add_topic_activity.clearFocus()
                }
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun findWords(keyWord: String) {
        Query().queryTranslateTopic(keyWord)
            .subscribe { topic ->
                if (topic.isNotEmpty())
                    CompositeDisposable().add(Query().querySearchWordsForTheme(getString(R.string.getWordsFromPhraseUrl) + topic)
                        .subscribe { listOfWords ->
                            Query().queryTranslationResult(listOfWords, "en-ru")
                                .flatMap { words -> Observable.fromIterable(words) }
                                .doOnNext { element ->
                                    adapter.add(NewTopicListRow(element))
                                }
                                .subscribe()
                        })
            }
    }

    private inner class SearchWordsTask(private val topic: String) : AsyncTask<Void, Array<Int>, Void>() {
        private val engWordsList = ArrayList<String>()
        private var isNowSearchingWordsFlag = true
        private var addNewElementInAdapterFlag = false

        override fun onPreExecute() {
            super.onPreExecute()
            adapter = GroupAdapter()
            recyclerview_new_words_activity_add_new_topic.adapter = adapter
            translation_status_textview_add_topic_activity.visibility = View.VISIBLE
        }

        @SuppressLint("SetJavaScriptEnabled")
        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                val ruUtfEncodeTopic = URLEncoder.encode(topic, "UTF-8")
                Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "encode: $ruUtfEncodeTopic")

                var engTopic: String
                if (ruUtfEncodeTopic != topic) {// если слово введенно на английском
                    val ruToEnTranslationHTML: Document =
                        Jsoup.connect("https://www.translate.ru/dictionary/ru-en/$ruUtfEncodeTopic").get()
                    engTopic = ruToEnTranslationHTML.select("span[class=ref_result]").text()

                    Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "TRANSLATION RESULT: $engTopic")

                    if (engTopic.contains(" "))
                        engTopic = engTopic.substring(0, engTopic.indexOf(" "))

                    if (engTopic == null) {
                        Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "engTopic == NULL"); return null
                    }

                    Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "trans res: $engTopic")
                } else {
                    engTopic = topic
                }

                val jsonWordsApi = WordRetrofitClient.instance.create(MyApi::class.java)

                CompositeDisposable().add(jsonWordsApi.getWordsForTheme(
                    getString(R.string.getWordsFromPhraseUrl) + engTopic
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { wordsList ->

                        Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "in composite disposable res")

                        for (enWord in wordsList) {
                            if (!enWord.word.contains("[0-9]")) engWordsList.add(enWord.word)
                        }
                        isNowSearchingWordsFlag = false

                    })

                while (isNowSearchingWordsFlag) try {
                    Thread.sleep(25)
                } catch (e: Exception) {
                }

                var wordsCount = engWordsList.size
                var i = 0

                for (word in engWordsList) {
                    val enToRuTranslationHTML: Document =
                        Jsoup.connect("https://www.translate.ru/dictionary/en-ru/$word").get()

                    val ruWordsList = enToRuTranslationHTML.select("span[class=ref_result]")

                    if (ruWordsList.isEmpty()) {
                        addNewElementInAdapterFlag = false
                        publishProgress(arrayOf(i, --wordsCount))
                        continue
                    }

                    val ruWord = ruWordsList.eachText().first().toString()
                    publishProgress(arrayOf(++i, wordsCount))

                    addNewElementInAdapterFlag = true
                    wordsPairsList.add(WordPair(word, ruWord))
                    Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "ru Word: $ruWord")
                }

            } catch (e: Exception) {
                Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, e.toString())
            }
            return null
        }

        @SuppressLint("SetTextI18n")
        override fun onProgressUpdate(vararg values: Array<Int>?) {
            translation_status_textview_add_topic_activity.text = "${values[0]?.first()}/${values[0]?.last()}"

            if (addNewElementInAdapterFlag) {
                try {
                    adapter.add(NewTopicListRow(wordsPairsList[values[0]?.first()!! - 1]))
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, e.toString())
                }
            }
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            push_btn_add_topic_activity.visibility = View.VISIBLE
            translation_status_textview_add_topic_activity.visibility = View.INVISIBLE
        }
    }


    private inner class SavingInDbTask : AsyncTask<Int, Int, Void>() {
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
            for (i in 0..(wordsPairsList.size - 1)) {
                publishProgress(i)
                try {
                    var wordsPairData = WordsPairData()
                    wordsPairData.engWord = wordsPairsList[i].originalWord
                    wordsPairData.translatedWord = wordsPairsList[i].translatedWord

                    wordsPairDataDao?.insert(wordsPairData)
                    wordsPairData = wordsPairDataDao?.getLastWordsPair()!!

                    wordsPairDataBaseHelper?.addWordsPairInDB(wordsPairData, topicData)
                    Log.d(
                        ADD_NEW_TOPIC_ACTIVITY_TAG,
                        "creating: ${wordsPairData.engWord} - ${wordsPairData.translatedWord}"
                    )
                    circle_progress_add_new_topic_activity.progress = progress++
                } catch (e: Exception) {
                }
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
            circle_progress_add_new_topic_activity.max = adapter.itemCount - 1
        }
    }
}
