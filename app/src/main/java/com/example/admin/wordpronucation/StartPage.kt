package com.example.admin.wordpronucation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

    private var mAudioManager: AudioManager? = null
    private var sharedPreferences: SharedPreferences? = null

    private var currentResult: String = ""
    private var wordsList: MutableList<String>? = null

    private var progressBarAsyncTask: ProgressBarAsyncTask? = null
    private var curIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)

        ripple_animation_startpage_activit.startRippleAnimation()
        ripple_animation_startpage_activit.visibility = View.INVISIBLE

        if (!checkPermissions())
            checkPermissions()

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        var bestRes = sharedPreferences?.getInt(getString(R.string.bestResultKey), -1)

        if (bestRes == -1) {
            val editor = sharedPreferences?.edit()
            editor?.putInt(getString(R.string.bestResultKey), 0)
            editor?.apply()
            bestRes = 0
        }

        best_result_textview_startpage_activity.text = bestRes.toString()

        wordsList = mutableListOf()

        wordsList?.add("example")
        wordsList?.add("car")
        wordsList?.add("flat")
        wordsList?.add("training")
        wordsList?.add("phone")
        wordsList?.add("circle")
        wordsList?.add("rectangle")
        wordsList?.add("london")
        wordsList?.add("russia")
        wordsList?.add("book")
        wordsList?.add("school")
        wordsList?.add("children")
        wordsList?.add("phone")
        wordsList?.add("request")
        wordsList?.add("remain")
        wordsList?.add("relax")
        wordsList?.add("vegetable")
        wordsList?.add("wood")
        wordsList?.add("workshop")
        wordsList?.add("young")
        wordsList?.add("yesterday")
        wordsList?.add("zone")
        wordsList?.add("version")
        wordsList?.add("use")
        wordsList?.add("unknown")
        wordsList?.add("universal")
        wordsList?.add("ugly")
        wordsList?.add("true")
        wordsList?.add("transformation")
        wordsList?.add("sweet")
        wordsList?.add("support")


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
            if (progressBarAsyncTask != null) progressBarAsyncTask?.setTrainingFlag(false)

            it.visibility = View.INVISIBLE
            enterTraining()
        }
    }

    private fun enterTraining() {
        wordsList?.shuffle()

        current_result_textview_startpage_activity.text = "0"

        curIndex = 0
        current_word_textview_startpage_activity.text = wordsList!![curIndex]

        unMuteSound()
        mTextToSpeech!!.speak(wordsList!![curIndex], TextToSpeech.QUEUE_FLUSH, null)

        val handler = Handler()
        handler.postDelayed({
            progressBarAsyncTask =  ProgressBarAsyncTask()
            progressBarAsyncTask!!.execute()
            muteSound()
            mSpeechRecognizer!!.startListening(mSpeechRecognizerIntent)

            ripple_animation_startpage_activit.visibility = View.VISIBLE

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

        private var containsFlag = false

        private var nextWordFlag = false


        override fun onPostExecute(result: Void?) {
            Log.d("test_async", "on post execute called")
            progressbar_startpage_activity.foregroundStrokeWidth = 10f
            start_textview_startpage_activity.visibility = View.VISIBLE
            ripple_animation_startpage_activit.visibility = View.INVISIBLE

            start_textview_startpage_activity.text = "Tap to restart"
            Toast.makeText(this@StartPage, "Wrong !\nYou say: $currentResult", Toast.LENGTH_LONG).show()

            super.onPostExecute(result)
        }
        override fun onPreExecute() {
            Log.d("test_async", "on pre execute called")
            progressbar_startpage_activity.foregroundStrokeWidth = 400f
            super.onPreExecute()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            progressbar_startpage_activity.progress = values[0]!!.toFloat()

            if (nextWordFlag) {
                ripple_animation_startpage_activit.visibility = View.INVISIBLE
                current_word_textview_startpage_activity.text = wordsList!![curIndex]
                mSpeechRecognizer?.stopListening()
                unMuteSound()
                mTextToSpeech!!.speak(wordsList!![curIndex], TextToSpeech.QUEUE_FLUSH, null)

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


            if (currentResult.toLowerCase().contains(wordsList!![curIndex].toLowerCase())) {
                containsFlag = true

            }
        }
        override fun doInBackground(vararg p0: Int?): Void? {
            while (training) {
                for (i in 0..80) {

                    publishProgress(i)

                    try {
                        Thread.sleep(4 * 10)
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
                    curIndex++
                    currentResult = ""

                    nextWordFlag = true

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


                /*Log.d("test_async", "loopIndex: $loopIndex, curIndex: $curIndex")
                if (!breakFlag) {
                    if (loopIndex < curIndex) {
                        loopIndex++; Log.d("test_async", "dont finish loop"); breakFlag = true
                    } else {
                        training = false; Log.d("test_async", "finish loop")
                    }
                } else breakFlag = false*/
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

        fun setTrainingFlag(tf: Boolean) {
            this.training = tf
        }
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
}
