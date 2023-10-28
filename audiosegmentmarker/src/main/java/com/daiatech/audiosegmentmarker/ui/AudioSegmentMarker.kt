package com.daiatech.audiosegmentmarker.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daiatech.audiosegmentmarker.data.AudioManager
import com.daiatech.audiosegmentmarker.waveform.AudioWaveform

@Composable
fun AudioSegmentMarker(
    audioFilePath: String,
    markers: List<Float>,
    onMarkerAdd: (Float) -> Unit
) {
    val context = LocalContext.current
    var amplitudes by remember { mutableStateOf(listOf<Int>()) }
    LaunchedEffect(Unit) {
        amplitudes = AudioManager.getAmplitudes(context, audioFilePath)
    }
    if (amplitudes.isNotEmpty()) {
        Row(Modifier.fillMaxWidth()) {
            AudioWaveform(
                amplitudes = amplitudes,
                waveformBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.border(2.dp, Color.Black),
                markers = markers,
                addMarker = onMarkerAdd
            )
        }
    }
}
