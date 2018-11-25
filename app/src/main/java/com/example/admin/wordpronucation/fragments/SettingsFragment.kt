package com.example.admin.wordpronucation.fragments

import android.app.Fragment
import android.app.FragmentManager
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.admin.wordpronucation.R
import com.example.admin.wordpronucation.retrofit.MyApi
import com.example.admin.wordpronucation.retrofit.TranslatorRetrofitClient
import com.example.admin.wordpronucation.retrofit.WordRetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*
import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.example.admin.wordpronucation.StartPage


class SettingsFragment: Fragment() {
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()

        sharedPreferences = activity.getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)

        change_language_layout_settings.setOnClickListener {
            showChangeLanguageDialog()
        }

        val topic = sharedPreferences?.getString(getString(R.string.keyWordKey), "")
        val timeLimit = sharedPreferences?.getInt(getString(R.string.timeLimitKey), -1)

        if (timeLimit == -1 || timeLimit == null) time_limit_edittext_settings.setText("2")
        else time_limit_edittext_settings.setText(timeLimit.toString())

        if (!topic.isNullOrEmpty()) topic_edittext_settings.setText(topic)

        save_settings.setOnClickListener {
            if (!time_limit_edittext_settings.text.isEmpty()) {
                changeTime()
            }

            if (!topic_edittext_settings.text.isEmpty()) {
                val jsonApi = TranslatorRetrofitClient.instance.create(MyApi::class.java)
                val text = topic_edittext_settings.text.toString()
                var correctTopicFlag = false

                CompositeDisposable().add(jsonApi.getTranslation("translate?key=" + getString(R.string.translatorApiKey) +
                        "&text=" + text + "&lang=" + "ru-en")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result -> correctTopicFlag = checkTopic(result.text[0])})
            }

            finishFragment()
        }
    }

    private fun changeTime() {
        try {
            val time = time_limit_edittext_settings.text.toString().toInt()
            if (time in 2..7) {
                val editor = sharedPreferences?.edit()
                editor?.putInt(getString(R.string.timeLimitKey), time)
                editor?.apply()
            }
            else {
                Toast.makeText(activity, "Wrong Time Limit!\n max = 5, min = 2", Toast.LENGTH_LONG).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Wrong Time Limit!", Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun checkTopic(topic: String): Boolean {
        val jsonApi = WordRetrofitClient.instance.create(MyApi::class.java)
        var correctTopicFlag = false

        CompositeDisposable().add(jsonApi.getWordsForTheme(
            getString(R.string.getWordsFromPhraseUrl) +
                    topic + "&max=5")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { words ->
                if (words.size != 5) {
                    Toast.makeText(activity, "Wrong Topic!", Toast.LENGTH_LONG).show()
                    topic_edittext_settings.text.clear()
                    return@subscribe
                }
                else {
                    Toast.makeText(activity, "Right Topic!", Toast.LENGTH_LONG).show()
                    val editor = sharedPreferences?.edit()
                    editor?.putString(getString(R.string.keyWordKey), topic_edittext_settings.text.toString())
                    editor?.apply()
                    correctTopicFlag = true
                }
            }
        )
        return correctTopicFlag
    }

    private fun showChangeLanguageDialog() {
        val langList: Array<String> = arrayOf("English", "Русский")
        val alertDialog = AlertDialog.Builder(activity, R.style.MyDialogTheme)
        alertDialog.setTitle("Choose language...")

        alertDialog.setSingleChoiceItems(langList, -1) { dialog, whichButton ->
            when (whichButton) {
                0 -> {setLocale("en"); activity.recreate() }
                1 -> {setLocale("ru"); activity.recreate() }
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
    }

    private fun finishFragment() {
        val ft = fragmentManager.beginTransaction()
        ft?.replace(R.id.recyclerview_fragment_startpage, android.app.Fragment())
        ft?.remove(this)
        ft?.commit()

        activity.recreate()
    }
}