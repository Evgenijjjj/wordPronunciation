package evgeny.example.admin.wordpronucation.retrofit

import evgeny.example.admin.wordpronucation.models.TranslationResult
import evgeny.example.admin.wordpronucation.models.Word
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface MyApi {
    @GET
    fun getWordsForTheme(@Url url: String): Observable<List<Word>>

    @GET
    fun getTranslation(@Url url: String): Observable<TranslationResult>
}