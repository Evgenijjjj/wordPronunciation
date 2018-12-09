package evgeny.example.admin.wordpronucation.vocabulary

import android.content.Context
import android.util.Log
import evgeny.example.admin.wordpronucation.R
import evgeny.example.admin.wordpronucation.database.VocabularyDataBase
import evgeny.example.admin.wordpronucation.database.helpers.WordsPairDataBaseHelper
import evgeny.example.admin.wordpronucation.models.WordPair
import java.io.BufferedReader
import java.io.InputStreamReader

const val VOCABULARY_LOG = "vocabulary_test"

class Vocabulary(private val ctx: Context) {

    fun getWordsWithTopic(topic: String): ArrayList<WordPair>?{
        if (topic == ctx.getString(R.string.random_words)) return getWordsPairsSandbox()
        if (topic == ctx.getString(R.string.elementary_level)) return getWordsPairsSandbox()
        if (topic == ctx.getString(R.string.middle_level)) return getWordsPairsSandbox()
        if (topic == ctx.getString(R.string.advanced_level)) return getWordsPairsSandbox()

        VocabularyDataBase.getInstance(ctx)?.TopicDataDao()?.getTopicWithName(topic) ?: return null
        return WordsPairDataBaseHelper(ctx).getAllWordPairsForTopic(VocabularyDataBase.getInstance(ctx)?.TopicDataDao()?.getTopicWithName(topic)!!)
    }

    private fun getWordsPairsSandbox(): ArrayList<WordPair>? {
        var list: ArrayList<WordPair> = arrayListOf()

        try {
            val reader = BufferedReader(
                InputStreamReader(ctx.assets.open("sandbox_pairs.txt"), "UTF-8")
            )
            var line: String? = reader.readLine()
            while (line != null) {
                if (!line.substring(0, line.indexOf("-")).contains(" ")) {
                    list.add(
                        WordPair(
                            line.substring(0, line.indexOf("-")),
                            line.substring(line.indexOf("-") + 2, line.length)
                        )
                    )
                    //Log.d(VOCABULARY_LOG, (i++).toString() + ":  ${line.substring(0, line.indexOf("-"))}==${line.substring(line.indexOf("-")+2, line.length)}")
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            Log.d(VOCABULARY_LOG, e.toString())
        }

        list.shuffle()

        for (pair in list) {
            Log.d(VOCABULARY_LOG, "${pair.originalWord} - ${pair.translatedWord}")
        }
        Log.d(VOCABULARY_LOG, list.size.toString())

        return list
    }
}