package evgeny.example.admin.wordpronucation.retrofit

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object TranslatorRetrofitClient {
    private var ourInstance: Retrofit? = null

    val instance: Retrofit
        get() {
            if (ourInstance == null) {
                ourInstance = Retrofit.Builder()
                    .baseUrl("https://translate.yandex.net/api/v1.5/tr.json/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            }
            return  ourInstance!!
        }
}