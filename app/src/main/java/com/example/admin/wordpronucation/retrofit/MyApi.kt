package com.example.admin.wordpronucation.retrofit

import com.example.admin.wordpronucation.models.Word
import retrofit2.http.GET

interface MyApi {
    @get:GET("words?rel_jjb=university")
    val words: io.reactivex.Observable<List<Word>>

}