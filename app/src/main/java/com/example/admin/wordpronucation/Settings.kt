package com.example.admin.wordpronucation

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.example.admin.wordpronucation.retrofit.MyApi
import com.example.admin.wordpronucation.retrofit.TranslatorRetrofitClient
import com.example.admin.wordpronucation.retrofit.WordRetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class Settings: Activity() {

    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)

        change_language_layout_settings.setOnClickListener {
            showChangeLanguageDialog()
        }

        val topic = sharedPreferences?.getString(getString(R.string.keyWordKey), "")
        val timeLimit = sharedPreferences?.getInt(getString(R.string.timeLimitKey), -1)

        if (timeLimit == -1) time_limit_edittext_settings.setText("2")
        else time_limit_edittext_settings.setText(timeLimit.toString())

        if (!topic.isNullOrEmpty()) topic_edittext_settings.setText(topic)

        save_settings.setOnClickListener {
            if (topic_edittext_settings.text.isEmpty()) {
                Toast.makeText(this, "Enter topic!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val jsonApi = TranslatorRetrofitClient.instance.create(MyApi::class.java)
            val text = topic_edittext_settings.text.toString()

            CompositeDisposable().add(jsonApi.getTranslation("translate?key=" + getString(R.string.translatorApiKey) +
                    "&text=" + text + "&lang=" + "ru-en")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result -> checkTopic(result.text[0])}
            )
        }
    }

    private fun checkTopic(topic: String) {
        val jsonApi = WordRetrofitClient.instance.create(MyApi::class.java)
        CompositeDisposable().add(jsonApi.getWordsForTheme(
            getString(R.string.getWordsFromPhraseUrl) +
                    topic + "&max=5")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { words ->
                if (words.size != 5) {
                    Toast.makeText(this, "Wrong Topic!", Toast.LENGTH_LONG).show()
                    topic_edittext_settings.text.clear()
                    return@subscribe
                }
                else {
                    try {
                        val time = time_limit_edittext_settings.text.toString().toInt()
                        if (time in 2..5) {
                            val editor = sharedPreferences?.edit()
                            editor?.putInt(getString(R.string.timeLimitKey), time)
                            editor?.apply()
                        }
                        else {
                            Toast.makeText(this, "Wrong Time Limit!\n max = 5, min = 2", Toast.LENGTH_LONG).show()
                            return@subscribe
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Wrong Time Limit!", Toast.LENGTH_LONG).show()
                        return@subscribe
                    }


                    Toast.makeText(this, "Right Topic!", Toast.LENGTH_LONG).show()
                    val editor = sharedPreferences?.edit()
                    editor?.putString(getString(R.string.keyWordKey), topic_edittext_settings.text.toString())
                    editor?.apply()
                    finish()
                }
            }
        )
    }

    private fun showChangeLanguageDialog() {
        val langList: Array<String> = arrayOf("English", "Русский")
        val alertDialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
        alertDialog.setTitle("Choose language...")

        alertDialog.setSingleChoiceItems(langList, -1) { dialog, whichButton ->
            when (whichButton) {
                0 -> {setLocale("en"); recreate() }
                1 -> {setLocale("ru"); recreate() }
            }
            dialog.dismiss()
        }

        val dialog = alertDialog.create()
        dialog.show()
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val editor = sharedPreferences?.edit()
        editor?.putString(getString(R.string.languageSP), lang)
        editor?.apply()
        finish()
    }
}