package com.thedroiddiv.audiosegmentmarker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thedroiddiv.audiosegmentmarker.app.ui.theme.AudioSegmentMarkerTheme
import com.thedroiddiv.audiosegmentmarker.ui.AudioSegmentMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {

    private val audioFilePath = MutableStateFlow<String?>(null)
    private val contentPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val file = File(filesDir, "temp.wav")
            if (!file.exists() && uri != null) {
                contentResolver.openInputStream(uri)?.run {
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int

                    while (this.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.close()
                    this.close()
                }
            }
            audioFilePath.update { file.path }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioSegmentMarkerTheme {
                // A surface container using the 'background' color from the theme
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        val audioFilePath by this@MainActivity.audioFilePath.collectAsState()
                        Button({ contentPicker.launch("audio/*") }) {
                            Text("Pick Audio")
                        }
                        audioFilePath?.let {
                            AudioSegmentMarker(it)
                        }
                    }
                }
            }
        }
    }
}

