package org.deepspeechdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.File
import java.io.FileOutputStream

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun copy() {
        val bufferSize = 1024
        val assetManager = assets
         assetManager.list("")?.let { assetFiles ->
             assetFiles.forEach {
                 val inputStream = assetManager.open(it)
                 val outputStream = FileOutputStream(File(filesDir, it))

                 try {
                     inputStream.copyTo(outputStream, bufferSize)
                 } finally {
                     inputStream.close()
                     outputStream.flush()
                     outputStream.close()
                 }
             }
         }


    }
}