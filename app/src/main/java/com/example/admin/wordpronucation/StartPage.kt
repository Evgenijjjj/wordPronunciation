package com.example.admin.wordpronucation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.admin.wordpronucation.models.WordPair
import com.example.admin.wordpronucation.retrofit.MyApi
import com.example.admin.wordpronucation.retrofit.TranslatorRetrofitClient
import com.example.admin.wordpronucation.retrofit.WordRetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_start_page.*
import java.util.*

class StartPage : Activity() {

    private var mTextToSpeech: TextToSpeech? = null
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mSpeechRecognizerIntent: Intent? = null

    private var mAudioManager: AudioManager? = null
    private var sharedPreferences: SharedPreferences? = null

    private var currentResult: String = ""
    private var wordsList: MutableList<String>? = null

    private var wordsPairsList: MutableList<WordPair>? = null

    private var progressBarAsyncTask: TrainingAsyncTask? = null
    private var curIndex = 0

    private lateinit var jsonApi: MyApi
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)

        setContentView(R.layout.activity_start_page)

        ripple_animation_startpage_activit.startRippleAnimation()
        ripple_animation_startpage_activit.visibility = View.INVISIBLE

        if (!checkPermissions())
            checkPermissions()

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setBestResToView()
        wordsList = mutableListOf()
        wordsPairsList = mutableListOf()

        mTextToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                mTextToSpeech?.language = Locale.UK
                mTextToSpeech?.setPitch(0.8f)
                mTextToSpeech?.setSpeechRate(0.8f)
            }
        })

        mTextToSpeech?.setOnUtteranceProgressListener(object :UtteranceProgressListener() {

            override fun onStart(p0: String?) {
                Log.d("synthesis_test", "onStart")
            }

            override fun onDone(p0: String?) {
               // muteSound()
                Log.d("synthesis_test", "onDone")
            }

            override fun onAudioAvailable(utteranceId: String?, audio: ByteArray?) {}

            override fun onError(p0: String?) {}

            override fun onError(utteranceId: String?, errorCode: Int) { super.onError(utteranceId, errorCode) }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) { super.onRangeStart(utteranceId, start, end, frame) }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                super.onStop(utteranceId, interrupted)
                Toast.makeText(this@StartPage, "Done", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginSynthesis(utteranceId: String?, sampleRateInHz: Int, audioFormat: Int, channelCount: Int) {
                super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount)
            }
        })

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mSpeechRecognizer!!.setRecognitionListener(SpeechRecognitionListener())
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_US")

        start_textview_startpage_activity.setOnClickListener {
            enterTraining()
        }

        settings_btn_startpage_activity.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }
    }

    private fun enterTraining() {
        wordsPairsList?.shuffle()

        if (wordsPairsList!!.isEmpty()) {
            Toast.makeText(this, "Ethernet error\nPleas turn on ethernet", Toast.LENGTH_SHORT).show()
            return
        }

        start_textview_startpage_activity.visibility = View.INVISIBLE
        current_word_framelayout_startpage_activity.visibility = View.VISIBLE
        current_result_textview_startpage_activity.text = "0"

        curIndex = 0
        current_word_textview_startpage_activity.text = wordsPairsList!![curIndex].originalWord
        current_translated_word_textview_startpage_activity.text = wordsPairsList!![curIndex].translatedWord

        unMuteSound()
        mTextToSpeech!!.speak(wordsPairsList!![curIndex].originalWord, TextToSpeech.QUEUE_FLUSH, null)

        val handler = Handler()
        handler.postDelayed({
            progressBarAsyncTask =  TrainingAsyncTask()
            progressBarAsyncTask!!.execute()
            muteSound()
            mSpeechRecognizer!!.startListening(mSpeechRecognizerIntent)

            ripple_animation_startpage_activit.visibility = View.VISIBLE

        }, 1200)
    }

    inner class SpeechRecognitionListener: RecognitionListener {
        override fun onReadyForSpeech(p0: Bundle?) {}

        override fun onRmsChanged(p0: Float) {}

        override fun onBufferReceived(p0: ByteArray?) {}

        override fun onPartialResults(p0: Bundle?) {}

        override fun onEvent(p0: Int, p1: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onEndOfSpeech() {}

        override fun onError(p0: Int) {}

        override fun onResults(p0: Bundle?) {
            val res = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            currentResult = res!![0]
            Log.d("test_async", "cur word : ${currentResult}")
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

    private inner class TrainingAsyncTask: AsyncTask<Int,Int,Void>() {
        private var training = true
        private var containsFlag = false
        private var nextWordFlag = false

        private var timeLimit = 2


        override fun onPostExecute(result: Void?) {
            Log.d("test_async", "on post execute called")
            progressbar_startpage_activity.foregroundStrokeWidth = 5f
            start_textview_startpage_activity.visibility = View.VISIBLE
            ripple_animation_startpage_activit.visibility = View.INVISIBLE

            Log.d("test_retrofit", wordsList?.size.toString())
            current_word_framelayout_startpage_activity.visibility = View.INVISIBLE

            start_textview_startpage_activity.text = getString(R.string.restartBtnText)
            Toast.makeText(this@StartPage, "Wrong !", Toast.LENGTH_LONG).show()

            super.onPostExecute(result)
        }
        override fun onPreExecute() {
            Log.d("test_async", "on pre execute called")
            progressbar_startpage_activity.foregroundStrokeWidth = 400f
            current_word_framelayout_startpage_activity.visibility = View.VISIBLE

            val time = sharedPreferences?.getInt(getString(R.string.timeLimitKey), -1)
            timeLimit = if (time == -1) 2 else time!!

            super.onPreExecute()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            progressbar_startpage_activity.progress = values[0]!!.toFloat()

            if (nextWordFlag) {
                ripple_animation_startpage_activit.visibility = View.INVISIBLE
                current_word_textview_startpage_activity.text = wordsPairsList!![curIndex].originalWord
                current_translated_word_textview_startpage_activity.text = wordsPairsList!![curIndex].translatedWord

                mSpeechRecognizer?.stopListening()
                unMuteSound()
                mTextToSpeech!!.speak(wordsPairsList!![curIndex].originalWord, TextToSpeech.QUEUE_FLUSH, null)

                Handler().postDelayed({
                    muteSound()
                    mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)


                    current_result_textview_startpage_activity.text = curIndex.toString()
                    ripple_animation_startpage_activit.visibility = View.VISIBLE

                    updateSharedPref()
0
                }, 1200)

                nextWordFlag = false
                return
            }


            if (currentResult.toLowerCase().contains(wordsPairsList!![curIndex].originalWord.toLowerCase())) {
                containsFlag = true

            }
        }
        override fun doInBackground(vararg p0: Int?): Void? {
            while (training) {
                for (i in 0..80) {

                    publishProgress(i)

                    try {
                        Thread.sleep(timeLimit.toLong() * 10)
                    } catch (e: Exception) {}

                }
                mAudioManager?.isMicrophoneMute = true
                Log.d("test_async", "MUTE")

                for (i in 80..100) {
                    publishProgress(i)

                    try {
                        Thread.sleep(7 * 10)
                    } catch (e: Exception) {}

                }

                mAudioManager?.isMicrophoneMute = false
                Log.d("test_async", "UNMUTE")

                if (containsFlag) {
                    Log.d("test_async", "CONTATINS: ${currentResult}")

                    containsFlag = false
                    nextWordFlag = true

                    curIndex++
                    currentResult = ""

                    publishProgress(100)

                    for (i in 0..50) {
                        try {
                            Thread.sleep(2 * 10)
                        } catch (e: Exception) {}
                    }
                }
                else {
                    training = false
                }
            }

            return null
        }

        private fun updateSharedPref() {
            if (best_result_textview_startpage_activity.text.toString().toInt() < current_result_textview_startpage_activity.text.toString().toInt()) {
                val editor = sharedPreferences?.edit()
                editor?.putInt(getString(R.string.bestResultKey), current_result_textview_startpage_activity.text.toString().toInt())
                editor?.apply()
                best_result_textview_startpage_activity.text = current_result_textview_startpage_activity.text
            }


        }
    }

    override fun onStart() {
        super.onStart()
        loadLocale()
        val topic = sharedPreferences?.getString(getString(R.string.keyWordKey), "education")
        topic_textview_startpage_activity.text = topic

        updateWords()
    }

    override fun onDestroy() {
        super.onDestroy()
        ripple_animation_startpage_activit.stopRippleAnimation()
    }

    private  fun muteSound(){
        var amanager= getSystemService(Context.AUDIO_SERVICE) as AudioManager
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true)
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        amanager.setStreamMute(AudioManager.STREAM_RING, true)
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
    }
    private fun unMuteSound(){
        var amanager= getSystemService(Context.AUDIO_SERVICE) as AudioManager
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false)
        amanager.setStreamMute(AudioManager.STREAM_ALARM, false)
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false)
        amanager.setStreamMute(AudioManager.STREAM_RING, false)
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false)
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
        Log.d("cdsvsdvs", "setlocale: $lang")
        setLocale(lang!!)

    }

    private fun fillWordsPairsList() {
        val jsonApi = TranslatorRetrofitClient.instance.create(MyApi::class.java)

        for (word in wordsList!!) {
            CompositeDisposable().add(jsonApi.getTranslation("translate?key=" + getString(R.string.translatorApiKey) +
                    "&text=" + word + "&lang=" + "en-ru")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result -> wordsPairsList?.add(WordPair(word, result.text[0]))}
            )
        }
    }

    private fun fetchWords(keyWord: String) {
        wordsList?.clear()
        Log.d("test_async", "keyWord = $keyWord")

        val retrofit = WordRetrofitClient.instance
        jsonApi = retrofit.create(MyApi::class.java)
        compositeDisposable = CompositeDisposable()

        compositeDisposable.add(
            jsonApi.getWordsForTheme(getString(R.string.getWordsFromPhraseUrl) + keyWord)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { words -> for (w in words) if (!w.word.contains("[0-9]")) wordsList?.add(w.word); fillWordsPairsList() }
        )
    }

    private fun updateWords(){
        val jsonApi = TranslatorRetrofitClient.instance.create(MyApi::class.java)
        val text = sharedPreferences?.getString(getString(R.string.keyWordKey), "education")

        CompositeDisposable().add(jsonApi.getTranslation("translate?key=" + getString(R.string.translatorApiKey) +
                "&text=" + text + "&lang=" + "ru-en")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result -> fetchWords(result.text[0])}
        )
    }
}
