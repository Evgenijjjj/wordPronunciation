package evgeny.example.admin.wordpronucation.training_part

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Toast
import evgeny.example.admin.wordpronucation.R
import evgeny.example.admin.wordpronucation.models.WordPair
import java.util.*
import kotlin.collections.ArrayList

const val TRAINING_LOG = "training_log"

class Training {
    private var mTextToSpeech: TextToSpeech? = null
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mSpeechRecognizerIntent: Intent? = null
    private var speakParams = HashMap<String, String>()

    private var mAudioManager: AudioManager? = null
    private var sharedPreferences: SharedPreferences? = null

    private var currentResult: String = ""

    private var curIndex = 0

    private var speechPronunciationSpeed: Float? = null

    private var streamVolume = 0

    private var listener: UiListener
    private val ctx: Context
    private var wordsPairsList: ArrayList<WordPair>

    private var isNowTextToSpeechWorking = true
    private var isRecognizerStarted = false


    constructor(ctx: Context, wordsPairsList: ArrayList<WordPair>, listener: UiListener) {
        this.listener = listener
        this.ctx = ctx
        this.wordsPairsList = wordsPairsList

        initServices()
    }

    fun startTraining(newPairs: ArrayList<WordPair>?) {
        if (newPairs != null) wordsPairsList = newPairs

        TrainingTask().execute()
    }

    fun stopTraining() {
        TrainingTask().cancel(false)
        muteSound(false)
    }

    private fun initServices() {
        sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.sharedPrefName), Context.MODE_PRIVATE)
        speechPronunciationSpeed = sharedPreferences?.getFloat(ctx.getString(R.string.speechPronunciationSpeed), 1f)

        mAudioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        streamVolume = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)

        speakParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "messageID = 1"

        mTextToSpeech = TextToSpeech(ctx, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                mTextToSpeech?.language = Locale.UK
                mTextToSpeech?.setPitch(0.8f)
                mTextToSpeech?.setSpeechRate(speechPronunciationSpeed!!)
            }
        })

        mTextToSpeech?.setOnUtteranceProgressListener(UtteranceListener())


        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(ctx)
        mSpeechRecognizer!!.setRecognitionListener(RecognitionListener())
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_US")
    }


    private inner class RecognitionListener : android.speech.RecognitionListener {
        override fun onReadyForSpeech(p0: Bundle?) {
            isRecognizerStarted = true
        }

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
            isRecognizerStarted = false
        }
    }

    private inner class UtteranceListener : UtteranceProgressListener() {
        override fun onStart(p0: String?) {
            muteSound(false)
            isNowTextToSpeechWorking = true
        }

        override fun onDone(p0: String?) {
            muteSound(true)
            isNowTextToSpeechWorking = false
            Log.d(TRAINING_LOG, "speech onDone")
        }

        override fun onAudioAvailable(utteranceId: String?, audio: ByteArray?) {}
        override fun onError(p0: String?) {}
        override fun onStop(utteranceId: String?, interrupted: Boolean) {}
    }

    @Suppress("DEPRECATION")
    private fun muteSound(mute: Boolean) {
        if (mute) {
            mAudioManager?.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
            mAudioManager?.setStreamMute(AudioManager.STREAM_ALARM, true)
            mAudioManager?.setStreamMute(AudioManager.STREAM_MUSIC, true)
            mAudioManager?.setStreamMute(AudioManager.STREAM_RING, true)
            mAudioManager?.setStreamMute(AudioManager.STREAM_SYSTEM, true)
        } else {
            mAudioManager?.setStreamMute(AudioManager.STREAM_NOTIFICATION, false)
            mAudioManager?.setStreamMute(AudioManager.STREAM_ALARM, false)
            mAudioManager?.setStreamMute(AudioManager.STREAM_MUSIC, false)
            mAudioManager?.setStreamMute(AudioManager.STREAM_RING, false)
            mAudioManager?.setStreamMute(AudioManager.STREAM_SYSTEM, false)
        }
    }

    private inner class TrainingTask : AsyncTask<Int, Int, Void>() {
        private var isNowTraining = true
        private var containsFlag = false
        private var nextWordFlag = false
        private var startFlag = true
        private var timeLimit = 3

        override fun onPreExecute() {
            super.onPreExecute()

            wordsPairsList.shuffle()
            curIndex = 0
            timeLimit = sharedPreferences?.getInt(ctx.getString(R.string.timeLimitKey), 3)!!

            listener.setVisibilityStartTextView(View.INVISIBLE)
            listener.setVisibilityCurrentWordFrameLayout(View.VISIBLE)
            listener.setTextCurrentResultTextView("0")
            listener.setProgressBarForegroundStrokeWidth(400f)

            listener.setTextCurrentWordTextView(wordsPairsList[curIndex].originalWord)
            listener.setTextCurrentTranslatedWordTextView(wordsPairsList[curIndex].translatedWord)

            muteSound(false)
            mTextToSpeech!!.speak(wordsPairsList[curIndex].originalWord, TextToSpeech.QUEUE_FLUSH, speakParams)
        }

        override fun onPostExecute(result: Void?) {
            listener.setProgressBarForegroundStrokeWidth(5f)
            listener.setVisibilityStartTextView(View.VISIBLE)
            listener.setVisibilityRippleAnimationView(View.INVISIBLE)
            listener.setVisibilityCurrentWordFrameLayout(View.INVISIBLE)
            listener.setTextStartTextView(ctx.getString(R.string.restartBtnText))
            listener.setTextCurrentResultTextView("0")
            Toast.makeText(ctx, ctx.getString(R.string.wrong), Toast.LENGTH_LONG).show()


            isNowTextToSpeechWorking = true
        }

        override fun onCancelled() {
            super.onCancelled()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            listener.setProgress(values[0]!!.toFloat())

            if (startFlag) {
                mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
                listener.setVisibilityRippleAnimationView(View.VISIBLE)
                startFlag = false
            }

            if (nextWordFlag) {
                listener.setVisibilityRippleAnimationView(View.INVISIBLE)
                listener.setTextCurrentWordTextView(wordsPairsList[curIndex].originalWord)
                listener.setTextCurrentTranslatedWordTextView(wordsPairsList[curIndex].translatedWord)
                listener.setTextCurrentResultTextView(curIndex.toString())

                mSpeechRecognizer?.stopListening()

                mTextToSpeech!!.speak(wordsPairsList[curIndex].originalWord, TextToSpeech.QUEUE_FLUSH, speakParams)

                listener.updateSharedPref()

                nextWordFlag = false
                return
            }


            if (currentResult.toLowerCase().contains(wordsPairsList[curIndex].originalWord.toLowerCase())) {
                containsFlag = true

            }
        }

        override fun doInBackground(vararg p0: Int?): Void? {
            while (isNowTraining) {
                while (isNowTextToSpeechWorking){ try { Thread.sleep(50) } catch (e: Exception) {} }

                publishProgress(100)
                while (!isRecognizerStarted) try { Thread.sleep(25) } catch (e: Exception) {}


                for (i in 0..80) {

                    publishProgress(i)

                    try {
                        Thread.sleep(timeLimit.toLong() * 10)
                    } catch (e: Exception) {
                    }

                }

                mAudioManager?.isMicrophoneMute = true

                for (i in 80..100) {
                    publishProgress(i)

                    try {
                        Thread.sleep(7 * 10)
                    } catch (e: Exception) {
                    }

                }

                mAudioManager?.isMicrophoneMute = false

                if (containsFlag) {
                    containsFlag = false
                    nextWordFlag = true
                    curIndex++
                    currentResult = ""

                    publishProgress(100)

                    for (i in 0..50) {
                        try {
                            Thread.sleep(2 * 10)
                        } catch (e: Exception) { }

                        startFlag = true
                    }
                } else {
                    isNowTraining = false
                }
            }

            return null
        }
    }
}