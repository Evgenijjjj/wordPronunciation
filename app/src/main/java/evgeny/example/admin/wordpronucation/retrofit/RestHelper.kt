package evgeny.example.admin.wordpronucation.retrofit

import android.content.Context
import android.util.Log
import evgeny.example.admin.wordpronucation.ADD_NEW_TOPIC_ACTIVITY_TAG
import evgeny.example.admin.wordpronucation.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RestHelper {
    private var ctx: Context

    constructor(ctx: Context) {
        this.ctx = ctx
    }

    fun translate(text: String, langPair: String): String {
        val jsonApi = TranslatorRetrofitClient.instance.create(MyApi::class.java)
        var res = ""

        CompositeDisposable().add(jsonApi.getTranslation("translate?key=" + ctx.getString(R.string.translatorApiKey) +
                "&text=" + text + "&lang=" + langPair)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result -> res = result.text[0]; Log.d(ADD_NEW_TOPIC_ACTIVITY_TAG, "topic in rest= ${result.text[0]}")})
        return res
    }

    fun findWordsForTopic(topic: String): ArrayList<String>? {
        val jsonApi = WordRetrofitClient.instance.create(MyApi::class.java)
        val res: ArrayList<String> = arrayListOf()

        CompositeDisposable().add(
            jsonApi.getWordsForTheme(ctx.getString(R.string.getWordsFromPhraseUrl) + topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { words ->
                    for (w in words) {
                        if (!w.word.contains("[0-9]") && !w.word.contains(' '))
                            res.add(w.word)
                    }
                }
        )
        Thread.sleep(1000)
        return res
    }
}