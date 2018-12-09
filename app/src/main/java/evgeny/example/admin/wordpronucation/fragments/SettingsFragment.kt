package evgeny.example.admin.wordpronucation.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*
import android.content.Context
import android.support.v4.app.Fragment
import android.util.Log
import android.widget.SeekBar
import evgeny.example.admin.wordpronucation.R
import kotlinx.android.synthetic.main.settings_fragment.*

const val SETTINGS_FRAGMENT_LOG = "settings_fragment_log"

class SettingsFragment: Fragment() {
    private var sharedPreferences: SharedPreferences? = null
    private var timerTime: Int = 3

    var callback: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        sharedPreferences = activity!!.getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)

        change_language_layout_settings.setOnClickListener {
            showChangeLanguageDialog()
        }

        val timeLimit = sharedPreferences?.getInt(getString(R.string.timeLimitKey), 3)
        textview_time_limit_settings.text = "$timeLimit ${getString(R.string.sec)}"
        val speechPronunciationSpeed = sharedPreferences?.getFloat(getString(R.string.speechPronunciationSpeed), 1f)

        when (speechPronunciationSpeed) {
            0.6f -> {
                seekbar_pronunciation_speed_settings.progress = 0; textview_pronunciation_speed_settings.text = "x0.6"
            }
            0.8f -> {
                seekbar_pronunciation_speed_settings.progress = 1; textview_pronunciation_speed_settings.text = "x0.8"
            }
            1f -> {
                seekbar_pronunciation_speed_settings.progress = 2; textview_pronunciation_speed_settings.text = "x1.0"
            }
            1.2f -> {
                seekbar_pronunciation_speed_settings.progress = 3; textview_pronunciation_speed_settings.text = "x1.2"
            }
            1.4f -> {
                seekbar_pronunciation_speed_settings.progress = 4; textview_pronunciation_speed_settings.text = "x1.4"
            }
        }

        when (timeLimit) {
            2 -> seekbar_time_limit_settings.progress = 0
            3 -> seekbar_time_limit_settings.progress = 1
            4 -> seekbar_time_limit_settings.progress = 2
            5 -> seekbar_time_limit_settings.progress = 3
            6 -> seekbar_time_limit_settings.progress = 4
        }

        seekbar_time_limit_settings.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                when (p1) {
                    0 -> {
                        textview_time_limit_settings.text = "2 ${getString(R.string.sec)}"
                        timerTime = 2
                    }
                    1 -> {
                        textview_time_limit_settings.text = "3 ${getString(R.string.sec)}"
                        timerTime = 3
                    }
                    2 -> {
                        textview_time_limit_settings.text = "4 ${getString(R.string.sec)}"
                        timerTime = 4
                    }
                    3 -> {
                        textview_time_limit_settings.text = "5 ${getString(R.string.sec)}"
                        timerTime = 5
                    }
                    4 -> {
                        textview_time_limit_settings.text = "6 ${getString(R.string.sec)}"
                        timerTime = 6
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })


        seekbar_pronunciation_speed_settings.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                Log.d(SETTINGS_FRAGMENT_LOG, "progress: $p1")
                when (p1) {
                    0 -> textview_pronunciation_speed_settings.text = "x0.6"
                    1 -> textview_pronunciation_speed_settings.text = "x0.8"
                    2 -> textview_pronunciation_speed_settings.text = "x1.0"
                    3 -> textview_pronunciation_speed_settings.text = "x1.2"
                    4 -> textview_pronunciation_speed_settings.text = "x1.4"
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        save_settings.setOnClickListener {
            changeTime()
            changeSpeechPronunciationSpeed()
            callback?.invoke()
        }
    }

    private fun changeTime() {
        val editor = sharedPreferences?.edit()
        editor?.putInt(getString(R.string.timeLimitKey), timerTime)
        editor?.apply()
    }

    private fun changeSpeechPronunciationSpeed() {
        val txt = textview_pronunciation_speed_settings.text.toString()
            .substring(textview_pronunciation_speed_settings.text.indexOf('x') + 1)

        try {
            val speed = txt.toFloat()

            val editor = sharedPreferences?.edit()
            editor?.putFloat(getString(R.string.speechPronunciationSpeed), speed)
            editor?.apply()
        } catch (e: Exception) {
        }
    }


    private fun showChangeLanguageDialog() {
        val langList: Array<String> = arrayOf("English", "Русский")
        val alertDialog = AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
        alertDialog.setTitle("Choose language...")

        alertDialog.setSingleChoiceItems(langList, -1) { dialog, whichButton ->
            when (whichButton) {
                0 -> {
                    setLocale("en"); activity!!.recreate()
                }
                1 -> {
                    setLocale("ru"); activity!!.recreate()
                }
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
}