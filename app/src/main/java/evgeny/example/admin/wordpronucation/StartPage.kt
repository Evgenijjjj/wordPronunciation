package evgeny.example.admin.wordpronucation

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.ads.AdRequest
import evgeny.example.admin.wordpronucation.fragments.SettingsFragment
import evgeny.example.admin.wordpronucation.fragments.TopicListFragment
import evgeny.example.admin.wordpronucation.models.WordPair
import evgeny.example.admin.wordpronucation.training_part.Training
import evgeny.example.admin.wordpronucation.training_part.UiListener
import evgeny.example.admin.wordpronucation.vocabulary.Vocabulary
import kotlinx.android.synthetic.main.activity_start_page.*
import java.util.*

const val START_PAGE_LOG = "start_page_log"

class StartPage : FragmentActivity(), UiListener {

    companion object {

    }

    private var sharedPreferences: SharedPreferences? = null

    private var wordsPairsList: ArrayList<WordPair>? = null

    private var training: Training? = null

    private val animationTime = 250L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)

        //init advertising ------
        val adRequest = AdRequest.Builder()
            .build()

        adViewBottom.loadAd(adRequest)
        //------

        sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)

        ripple_animation_startpage_activit.startRippleAnimation()
        ripple_animation_startpage_activit.visibility = View.INVISIBLE

        if (!checkPermissions())
            checkPermissions()

        setBestResToView()

        open_lv_btn_startpage.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.settingsFragment))
            var delay = 0L

            if (fragment != null) {
                close_settings_btn_startpage.performClick()
                delay = animationTime
            }

            val newFragment = TopicListFragment()
            newFragment.callback = {
                val topic = sharedPreferences?.getString(getString(R.string.keyWordKey), getString(R.string.random_words))
                topic_textview_startpage_activity.text = topic
                updateWords(topic!!)
                close_lv_btn_startpage.performClick()
            }

            Handler().postDelayed({

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_topics, R.anim.exit_topics)
                    .replace(
                        R.id.recyclerview_fragment_startpage,
                        newFragment,
                        getString(R.string.topicListFragment)
                    )
                    .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()

                close_lv_btn_startpage.isClickable = true
                open_lv_btn_startpage.isClickable = false

                YoYo.with(Techniques.ZoomOutLeft)
                    .duration(animationTime)
                    .playOn(open_lv_btn_startpage)

                Handler().postDelayed({
                    close_lv_btn_startpage.visibility = View.VISIBLE
                    YoYo.with(Techniques.FadeIn)
                        .duration(animationTime)
                        .playOn(close_lv_btn_startpage)
                }, animationTime)

            }, delay)
        }

        close_lv_btn_startpage.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.topicListFragment)) ?: return@setOnClickListener
            val shadow = TopicListFragment()

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.exit_topics, R.anim.exit_topics)
                .replace(R.id.recyclerview_fragment_startpage, shadow)
                .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()

            open_lv_btn_startpage.isClickable = true
            close_lv_btn_startpage.isClickable = false

            YoYo.with(Techniques.FadeOut)
                .duration(animationTime)
                .playOn(close_lv_btn_startpage)

            Handler().postDelayed({
                supportFragmentManager.beginTransaction().remove(shadow).commit()
                supportFragmentManager.beginTransaction().remove(fragment).commit()

                YoYo.with(Techniques.ZoomInLeft)
                    .duration(animationTime)
                    .playOn(open_lv_btn_startpage)
            }, animationTime)
        }

        settings_btn_startpage_activity.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.topicListFragment))
            var delay = 0L

            it.isClickable = false

            if (fragment!= null) {
                close_lv_btn_startpage.performClick()
                delay = animationTime
            }

            val newFragment = SettingsFragment()
            newFragment.callback = {
                loadLocale()
                close_settings_btn_startpage.performClick()
            }

            Handler().postDelayed({

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_settings, R.anim.enter_settings)
                    .replace(
                        R.id.recyclerview_fragment_startpage,
                        newFragment,
                        getString(R.string.settingsFragment)
                    )
                    .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()

                YoYo.with(Techniques.ZoomOut)
                    .duration(animationTime)
                    .playOn(settings_btn_startpage_activity)

                Handler().postDelayed({
                    close_settings_btn_startpage.isClickable = true
                    close_settings_btn_startpage.visibility = View.VISIBLE
                    YoYo.with(Techniques.FadeIn)
                        .duration(animationTime)
                        .playOn(close_settings_btn_startpage)
                }, animationTime)

            }, delay)
        }

        close_settings_btn_startpage.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.settingsFragment)) ?: return@setOnClickListener
            val shadow = SettingsFragment()

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.exit_settings, R.anim.exit_settings)
                .replace(R.id.recyclerview_fragment_startpage, shadow)
                .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()

            close_settings_btn_startpage.isClickable = false

            YoYo.with(Techniques.FadeOut)
                .duration(animationTime)
                .playOn(close_settings_btn_startpage)

            Handler().postDelayed({
                supportFragmentManager.beginTransaction().remove(shadow).commit()
                supportFragmentManager.beginTransaction().remove(fragment).commit()
                settings_btn_startpage_activity.isClickable = true

                YoYo.with(Techniques.ZoomIn)
                    .duration(animationTime)
                    .playOn(settings_btn_startpage_activity)
            }, animationTime)
        }

        activity_start_page.setOnTouchListener{ _, m: MotionEvent ->
           // Log.d(START_PAGE_LOG, "touch at x = ${m.x}, y = ${m.y}, action: ${m.action}, // ${MotionEvent.ACTION_UP}")
            if (m.action == MotionEvent.ACTION_UP) {
                when {
                    supportFragmentManager.findFragmentByTag(getString(R.string.settingsFragment)) != null -> close_settings_btn_startpage.performClick()
                    supportFragmentManager.findFragmentByTag(getString(R.string.topicListFragment)) != null -> close_lv_btn_startpage.performClick()
                    else -> false
                }
            }
            true
        }

        start_textview_startpage_activity.setOnClickListener {
            training?.startTraining(null)
        }

    }

    private fun checkPermissions(): Boolean {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.INTERNET)
        permissions.add(Manifest.permission.RECORD_AUDIO)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        var result: Int
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 100)
            return false
        }
        return true
    }


    override fun onStart() {
        super.onStart()
        try {
            loadLocale()
            val topic = sharedPreferences?.getString(getString(R.string.keyWordKey), getString(R.string.random_words))
            topic_textview_startpage_activity.text = topic
            updateWords(topic!!)

            training = Training(this, wordsPairsList!!, this)
        } catch (e: Exception) {
            Log.d(START_PAGE_LOG, "on start ex: $e")
        }
        animateStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        ripple_animation_startpage_activit.stopRippleAnimation()
    }

    override fun onPause() {
        super.onPause()
        training?.stopTraining()
    }

    private fun setBestResToView() {
        var bestRes = sharedPreferences?.getInt(getString(R.string.bestResultKey), -1)

        if (bestRes == -1) {
            val editor = sharedPreferences?.edit()
            editor?.putInt(getString(R.string.bestResultKey), 0)
            editor?.apply()
            bestRes = 0
        }

        best_result_textview_startpage_activity.text = bestRes.toString()
    }

    private fun showChangeLanguageDialog() {
        val langList: Array<String> = arrayOf("English", "Русский")
        val alertDialog = AlertDialog.Builder(this@StartPage, R.style.MyDialogTheme)
        alertDialog.setTitle("Choose language...")

        alertDialog.setSingleChoiceItems(langList, -1) { dialog, whichButton ->
            when (whichButton) {
                0 -> {
                    setLocale("en"); recreate()
                }
                1 -> {
                    setLocale("ru"); recreate()
                }
            }
            dialog.dismiss()
        }

        val dialog = alertDialog.create()
        dialog.show()
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        val config = this.resources.configuration
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val editor = sharedPreferences?.edit()
        editor?.putString(getString(R.string.languageSP), lang)
        editor?.apply()
    }

    private fun loadLocale() {
        val lang = sharedPreferences?.getString(getString(R.string.languageSP), "")
        if (lang.isNullOrEmpty()) {
            showChangeLanguageDialog()
            return
        }
        setLocale(lang!!)

    }

    private fun updateWords(topic: String) {
        wordsPairsList = Vocabulary(this).getWordsWithTopic(topic)
        if (wordsPairsList == null) {
            Toast.makeText(this, getString(R.string.thisTopicIsRemoved), Toast.LENGTH_LONG).show()
        }
    }

    private fun animateStart() {
        YoYo.with(Techniques.FadeIn)
            .duration(1200)
            .playOn(progressbar_startpage_activity)

        YoYo.with(Techniques.FadeIn)
            .duration(1200)
            .playOn(current_result_box_startpage)

        YoYo.with(Techniques.FadeIn)
            .duration(1200)
            .playOn(best_result_box_startpage)

        YoYo.with(Techniques.FadeIn)
            .duration(1200)
            .playOn(start_textview_startpage_activity)

        YoYo.with(Techniques.FadeIn)
            .duration(1200)
            .playOn(open_lv_btn_startpage)

        YoYo.with(Techniques.FadeIn)
            .duration(1200)
            .playOn(settings_btn_startpage_activity)
    }

    override fun setVisibilityStartTextView(v: Int) {
        start_textview_startpage_activity.visibility = v
    }

    override fun setVisibilityCurrentWordFrameLayout(v: Int) {
        current_word_framelayout_startpage_activity.visibility = v
    }

    override fun setVisibilityRippleAnimationView(v: Int) {
        ripple_animation_startpage_activit.visibility = v
    }

    override fun setTextCurrentResultTextView(text: String) {
        current_result_textview_startpage_activity.text = text
    }

    override fun setTextCurrentWordTextView(text: String) {
        current_word_textview_startpage_activity.text = text
    }

    override fun setTextCurrentTranslatedWordTextView(text: String) {
        current_translated_word_textview_startpage_activity.text = text
    }

    override fun setProgress(progress: Float) {
        progressbar_startpage_activity.progress = progress
    }

    override fun updateSharedPref() {
        if (best_result_textview_startpage_activity.text.toString().toInt() < current_result_textview_startpage_activity.text.toString().toInt()) {
            val editor = sharedPreferences?.edit()
            editor?.putInt(
                getString(R.string.bestResultKey),
                current_result_textview_startpage_activity.text.toString().toInt()
            )
            editor?.apply()
            best_result_textview_startpage_activity.text = current_result_textview_startpage_activity.text
        }
    }

    override fun setProgressBarForegroundStrokeWidth(width: Float) {
        progressbar_startpage_activity.foregroundStrokeWidth = width
    }

    override fun setTextStartTextView(text: String) {
        start_textview_startpage_activity.text = text
    }
}