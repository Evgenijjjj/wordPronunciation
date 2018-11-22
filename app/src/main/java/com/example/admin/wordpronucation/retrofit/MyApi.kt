package com.example.admin.wordpronucation.retrofit

import com.example.admin.wordpronucation.models.Word
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface MyApi {
    @GET
    fun getWordsForTheme(@Url url: String): Observable<List<Word>>
}