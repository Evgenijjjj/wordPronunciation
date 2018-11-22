package com.example.admin.wordpronucation

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import com.example.admin.wordpronucation.retrofit.MyApi
import com.example.admin.wordpronucation.retrofit.WordRetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_settings.*

class Settings: Activity() {

    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)

        val topic = sharedPreferences?.getString(getString(R.string.keyWordKey), "")

        if (!topic.isNullOrEmpty()) topic_edittext_settings.setText(topic)

        save_settings.setOnClickListener {
            val jsonApi = WordRetrofitClient.instance.create(MyApi::class.java)

            CompositeDisposable().add(jsonApi.getWordsForTheme(
                getString(R.string.getWordsWithThemeUrl) +
                        topic_edittext_settings.text.toString() + "&max=5")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { words ->
                    if (words.size != 5) {
                        Toast.makeText(this, "Wrong Topic!", Toast.LENGTH_LONG).show()
                        topic_edittext_settings.text.clear()
                        return@subscribe
                    }
                    else {
                        Toast.makeText(this, "Right Topic!", Toast.LENGTH_LONG).show()
                        val editor = sharedPreferences?.edit()
                        editor?.putString(getString(R.string.keyWordKey), topic_edittext_settings.text.toString())
                        editor?.apply()
                        finish()
                    }
                }
            )
        }
    }

}