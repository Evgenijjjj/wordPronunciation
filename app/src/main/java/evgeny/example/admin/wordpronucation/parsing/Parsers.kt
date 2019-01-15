package evgeny.example.admin.wordpronucation.parsing

import android.util.Log
import evgeny.example.admin.wordpronucation.models.Word
import evgeny.example.admin.wordpronucation.models.WordPair
import org.jsoup.Jsoup
import java.net.URLEncoder

const val PARSING_LOG = "parsers_log"
class Parsers {
    fun scrapTranslationResultForTopic(topic: String): String {
        val encodedTopic = URLEncoder.encode(topic, "UTF-8")

        /** check that the word has already been entered in English */
        if (encodedTopic == topic) return topic

        var translationResult = ""

        val html = Jsoup.connect("https://www.translate.ru/dictionary/ru-en/$encodedTopic")
            .get()

        translationResult = html.select("span[class=ref_result]").eachText().first().toString()

        Log.d(PARSING_LOG, "topic: $translationResult")
        return translationResult
    }

    fun scrapTranslationResult(wordsList: List<Word>, pair: String): List<WordPair> {
        val list: ArrayList<WordPair> = arrayListOf()

        for (word in wordsList) {
            val encodedWord = URLEncoder.encode(word.word, "UTF-8")
            var translationResult = ""

            val html = Jsoup.connect("https://www.translate.ru/dictionary/$pair/$encodedWord")
                .get()

            translationResult = html.select("span[class=ref_result]").eachText().first().toString()

            if (!translationResult.contains("[0-9]") && translationResult.isNotEmpty())
                list.add(WordPair(word.word,translationResult))
        }
        return list
    }
}