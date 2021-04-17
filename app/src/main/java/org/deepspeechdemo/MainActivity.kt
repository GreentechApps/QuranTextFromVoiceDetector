package org.deepspeechdemo

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


class MainActivity : AppCompatActivity() {
    private var model: DeepSpeechModel? = null

    private var transcriptionThread: Thread? = null
    private var isRecording: AtomicBoolean = AtomicBoolean(false)

    private val TFLITE_MODEL_FILENAME = "output_graph_imams_tusers_v2.tflite"
    private val SCORER_FILENAME = "quran.scorer"

    private val ayah = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ"
    private var detectableWords = ArrayList<Word>()

    private fun checkAudioPermission() {
        // Permission is automatically granted on SDK < 23 upon installation.
        if (Build.VERSION.SDK_INT >= 23) {
            val permission = Manifest.permission.RECORD_AUDIO

            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 3)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAudioPermission()
        generateDetectableText()
        // Create application data directory on the device
        //val modelsPath = getExternalFilesDir(null).toString()
        //status.text = "Ready. Copy model files to \"$modelsPath\" if running for the first time.\n"
    }

    private fun generateDetectableText(){
        val array = ayah.split(" ") //Array(ayah.length) {ayah[it].toString()}.reversed()
        array.forEachIndexed {index, element ->
            val word = Word(index,element,false);
            detectableWords.add(word)
        }
    }

    private fun detectWord(text: String) : Boolean{
        if(text.isBlank() || text.isEmpty()){
            return false
        }
        Log.d("LISTNER",text)
        detectableWords.forEachIndexed{ index, element ->
            Log.d("LISTNER","2 ${element.index} ${element.text} ${element.isDetected}")
            if(!element.isDetected  ){
                val detected = ayah.split(" ")
                detected.forEachIndexed { detectedIndex, detectedElement ->
                    if(element.text.contains(detectedElement)) {
                        Log.d("LISTNER", "3 ${element.text} : ${text}")
                        detectableWords[index].isDetected = true
                    }
                }

            }
        }

        var sentence = ""
        detectableWords.filter {value -> value.isDetected}.forEach {
            sentence += it.text + " "
        }
        detection.text = sentence

        return false
    }


    private fun transcribe() {
        // We read from the recorder in chunks of 2048 shorts. With a model that expects its input
        // at 16000Hz, this corresponds to  2048/16000 = 0.128s or 128ms.
        val audioBufferSize = 20048
        val audioData = ShortArray(audioBufferSize)

        runOnUiThread { btnStartInference.text = "Stop Recording" }

        model?.let { model ->
            val streamContext = model.createStream()

            val recorder = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    model.sampleRate(),
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioBufferSize
            )
            recorder.startRecording()

            while (isRecording.get()) {
                recorder.read(audioData, 0, audioBufferSize)
                model.feedAudioContent(streamContext, audioData, audioData.size)
                val decoded = model.intermediateDecode(streamContext)

                runOnUiThread {
                    detectWord(decoded)
                    transcription.text = decoded
                }
            }

            val decoded = model.finishStream(streamContext)
               runOnUiThread {
                   btnStartInference.text = "Start Recording"
                   transcription.text = decoded
               }

               recorder.stop()
               recorder.release()

        }
    }

    private fun writeFileToStorage(fileName: String) {
        val assetManager: AssetManager = this.assets
        if (File(getFilePath(fileName)).exists()) {
            return
        }
        try {
            assetManager.open(fileName).use { input ->
                FileOutputStream(getFilePath(fileName)).use { output ->
                    val buffer = ByteArray(input.available())
                    var length: Int
                    while (input.read(buffer).also { length = it } != -1) {
                        output.write(buffer, 0, length)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun getFilePath(fileName: String): String {
        return "$filesDir/$fileName"
    }
    private fun createModel(): Boolean {

        //val modelsPath = getExternalFilesDir(null).toString()
        val tfliteModelPath = getFilePath(TFLITE_MODEL_FILENAME) //"$modelsPath/$TFLITE_MODEL_FILENAME"
        val scorerPath = getFilePath(SCORER_FILENAME) //"$modelsPath/$SCORER_FILENAME"

        if(!File(tfliteModelPath).exists()){
            writeFileToStorage(TFLITE_MODEL_FILENAME)
        }

        if(!File(scorerPath).exists()){
            writeFileToStorage(SCORER_FILENAME)
        }

        for (path in listOf(tfliteModelPath, scorerPath)) {
            if (!File(path).exists()) {
                detection.append("Model creation failed: $path does not exist.\n")
                return false
            }
        }

        model = DeepSpeechModel(tfliteModelPath)
        model?.enableExternalScorer(scorerPath)

        return true
    }

    private fun startListening() {
        if (isRecording.compareAndSet(false, true)) {
            transcriptionThread = Thread(Runnable { transcribe() }, "Transcription Thread")
            transcriptionThread?.start()
        }
    }

    private fun stopListening() {
        isRecording.set(false)
    }

    fun onClear(v: View?) {
        stopListening()
        detectableWords.forEachIndexed{ index,element ->
            detectableWords[index].isDetected = false
        }
        detection.text = ""
    }

    fun onRecordClick(v: View?) {
        if (model == null) {
            if (!createModel()) {
                return
            }
        }

        if (isRecording.get()) {
            stopListening()
        } else {
            startListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (model != null) {
            model?.freeModel()
        }
    }
}
