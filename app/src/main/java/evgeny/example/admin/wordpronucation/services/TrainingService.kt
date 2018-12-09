package evgeny.example.admin.wordpronucation.services

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.AsyncTask
import android.os.IBinder
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import evgeny.example.admin.wordpronucation.models.WordPair
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Exception


const val TRAINING_SERVICE_LOG = "training_service"
//CHECK INTERNET CONNECTION (FOR FUTURE)
/*abstract class TrainingService : Service() {

    private var mAudioManager: AudioManager? = null
    private var sharedPreferences: SharedPreferences? = null

    private var wordsPairsList: ArrayList<WordPair>? = null

    private var mTextToSpeech: TextToSpeech? = null
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mSpeechRecognizerIntent: Intent? = null
    private var speakParams = HashMap<String, String>()

    private var curIndex = 0

    private var speechPronunciationSpeed: Float? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d(TRAINING_SERVICE_LOG, "onBind")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.d(TRAINING_SERVICE_LOG, "OnUnbind")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TRAINING_SERVICE_LOG, "OnCreate")

        /*sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE)
        speechPronunciationSpeed = sharedPreferences?.getFloat(getString(R.string.speechPronunciationSpeed), 1f)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        speakParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "messageID = 1"

        mTextToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                mTextToSpeech?.language = Locale.UK
                mTextToSpeech?.setPitch(0.8f)
                mTextToSpeech?.setSpeechRate(speechPronunciationSpeed!!)
            }
        })

        mTextToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(p0: String?) {
                Log.d("synthesis_test", "UNMUTE")
                //unMuteSound() // мешает звук вкл спич рекогнайзера если синтезирует > 2 сек
            }

            override fun onDone(p0: String?) {
                // muteSound() // мешает звук вкл спич рекогнайзера если синтезирует > 2 сек
                Log.d("synthesis_test", "MUTE")
            }
            override fun onError(p0: String?) {}
            override fun onStop(utteranceId: String?, interrupted: Boolean) {}
        })*/
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Log.d(TRAINING_SERVICE_LOG, "OnStartCommand")

        readFlags(flags)
        //val mr = MyRun(startId)
        //Thread(mr).start()
        val tr = TrainingAsyncTask(startId)
        tr.execute()
        return START_NOT_STICKY
    }

    private fun readFlags(flags: Int) {
        if (flags and Service.START_FLAG_REDELIVERY == Service.START_FLAG_REDELIVERY)
            Log.d(TRAINING_SERVICE_LOG, "START_FLAG_REDELIVERY")
        if (flags and Service.START_FLAG_RETRY == Service.START_FLAG_RETRY)
            Log.d(TRAINING_SERVICE_LOG, "START_FLAG_RETRY")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TRAINING_SERVICE_LOG, "OnDestroy")
    }

    private inner class TrainingAsyncTask(private val startId: Int) : AsyncTask<Int, Int, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            Log.d(TRAINING_SERVICE_LOG, "OnPreExecute id = $startId")


        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Log.d(TRAINING_SERVICE_LOG, "OnPostExecute")
            stopService()
        }

        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                Thread.sleep(5000)
            } catch (e: Exception) {
            }

            return null
        }

        override fun onCancelled() {
            super.onCancelled()
            Log.d(TRAINING_SERVICE_LOG, "OnCancelled")
        }

        fun stopService() {
            Log.d(
                TRAINING_SERVICE_LOG, "MyRun#" + startId + " end, stopSelfResult("
                        + startId + ") = " + stopSelfResult(startId)
            )
        }
    }

    internal inner class MyRun(var startId: Int) : Runnable {

        init {
            Log.d(TRAINING_SERVICE_LOG, "MyRun#$startId create")
        }

        override fun run() {
            Log.d(TRAINING_SERVICE_LOG, "MyRun#$startId start")
            try {
                TimeUnit.SECONDS.sleep(5)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            stop()
        }

        fun stop() {
            Log.d(
                TRAINING_SERVICE_LOG, "MyRun#" + startId + " end, stopSelfResult("
                        + startId + ") = " + stopSelfResult(startId)
            )
        }
    }
}*/