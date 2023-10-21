package com.thedroiddiv.audiosegmentmarker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.thedroiddiv.audiosegmentmarker.app.ui.theme.AudioSegmentMarkerTheme
import com.thedroiddiv.audiosegmentmarker.ui.AudioSegmentMarker
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            AudioSegmentMarkerTheme {
                // A surface container using the 'background' color from the theme

                var audioFilePath by remember { mutableStateOf<String?>(null) }
                val contentPicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { u ->
                    u?.let { uri ->
                        contentResolver.openInputStream(uri)?.run {
                            val file = File(filesDir, "temp.wav")
                            val outputStream = FileOutputStream(file)
                            // You can adjust the buffer size as needed
                            val buffer = ByteArray(4 * 1024)
                            var bytesRead: Int

                            while (this.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }

                            outputStream.close()
                            this.close()

                            audioFilePath = file.path
                        }
                    }
                }

                audioFilePath?.let {
                    Column(Modifier.fillMaxSize()) {
                        AudioSegmentMarker(it)
                        Button({ audioFilePath = null }) {
                            Text("Delete")
                        }
                    }
                } ?: run {
                    Button({ contentPicker.launch("audio/*") }) {
                        Text("Pick Audio")
                    }
                }
            }
        }
    }
}

