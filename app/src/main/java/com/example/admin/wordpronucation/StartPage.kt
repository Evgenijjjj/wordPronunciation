package com.example.admin.wordpronucation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_start_page.*
import java.util.*

class StartPage : Activity() {
    private var mTextToSpeech: TextToSpeech? = null
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mSpeechRecognizerIntent: Intent? = null

    private var currentResult: String = ""
    private var wordsList: MutableList<String>? = null

    private var progressBarAsyncTask: ProgressBarAsyncTask? = null
    private var curIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)

        if (!checkPermissions())
            checkPermissions()

        wordsList = mutableListOf()

        wordsList?.add("example")
        wordsList?.add("car")
        wordsList?.add("flat")
        wordsList?.add("training")
        wordsList?.add("phone")


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
        mSpeechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "uk")

        start_textview_startpage_activity.setOnClickListener {
            if (progressBarAsyncTask != null) progressBarAsyncTask?.setTrainingFlag(false)

            it.visibility = View.INVISIBLE
            enterTraining()
        }
    }

    private fun enterTraining() {
        current_result_textview_startpage_activity.text = "0"
        unMuteSound()
        mTextToSpeech!!.speak(wordsList!![curIndex], TextToSpeech.QUEUE_FLUSH, null)

        val handler = Handler()
        handler.postDelayed({
            progressBarAsyncTask =  ProgressBarAsyncTask()
            progressBarAsyncTask!!.execute()
            muteSound()
            mSpeechRecognizer!!.startListening(mSpeechRecognizerIntent)


        }, 2000)
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

    private inner class ProgressBarAsyncTask: AsyncTask<Int,Int,Void>() {
        private var training = true
        private var recognized = false
        private var nextWordFlag = false
        private var loopIndex = 0


        override fun onPostExecute(result: Void?) {
            Log.d("test_async", "on post execute called")
            start_textview_startpage_activity.visibility = View.VISIBLE
            super.onPostExecute(result)
        }
        override fun onPreExecute() {
            Log.d("test_async", "on pre execute called")
            super.onPreExecute()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            progressbar_startpage_activity.progress = values[0]!!.toFloat()

            if (currentResult.toLowerCase().contains(wordsList!![curIndex])) {
                recognized = true
                curIndex++
                currentResult = ""
                Log.d("test_async", "contains")

                current_word_textview_startpage_activity.text = wordsList!![curIndex]
                mSpeechRecognizer?.stopListening()
                unMuteSound()
                mTextToSpeech!!.speak(wordsList!![curIndex], TextToSpeech.QUEUE_FLUSH, null)

                Handler().postDelayed({
                    muteSound()

                    mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)

                    Thread.sleep(200)

                    recognized = false
                    nextWordFlag = true

                    current_result_textview_startpage_activity.text = (curIndex + 1).toString()

                }, 2000)
            }
        }
        override fun doInBackground(vararg p0: Int?): Void? {
            while (training) {
                for (i in 0..100) {

                    if (nextWordFlag) {
                        nextWordFlag = false
                        Log.d("test_async", "break")
                        break
                    }

                    if (!recognized) {
                        publishProgress(i)
                    }


                    try {
                        Thread.sleep(4 * 10)
                    } catch (e: Exception) {}

                }
                Log.d("test_async", "finish loop")
                if (loopIndex < curIndex) loopIndex++
                else training = false

            }

            return null
        }

        fun setTrainingFlag(tf: Boolean) {
            this.training = tf
        }
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
}
