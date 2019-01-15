package evgeny.example.admin.wordpronucation.parsing

import evgeny.example.admin.wordpronucation.models.Word
import evgeny.example.admin.wordpronucation.models.WordPair
import evgeny.example.admin.wordpronucation.retrofit.MyApi
import evgeny.example.admin.wordpronucation.retrofit.WordRetrofitClient
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class Query {
    fun querySearchWordsForTheme(url: String): Observable<List<Word>> {
        val jsonWordsApi = WordRetrofitClient.instance.create(MyApi::class.java)
        return jsonWordsApi.getWordsForTheme(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun queryTranslationResult(wordsList: List<Word>, pair: String): Observable<List<WordPair>> {
        return Observable.create(ObservableOnSubscribe<List<WordPair>> { subscriber ->
            subscriber.onNext(Parsers().scrapTranslationResult(wordsList, pair))
            subscriber.onComplete()
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun queryTranslateTopic(topic: String): Single<String> {
        return Single.fromCallable {
            Parsers().scrapTranslationResultForTopic(topic)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}