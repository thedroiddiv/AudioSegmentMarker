package com.thedroiddiv.audiosegmentmarker.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.thedroiddiv.audiosegmentmarker.data.AudioManager
import com.thedroiddiv.audiosegmentmarker.waveform.AudioWaveform

@Composable
fun AudioSegmentMarker(
    audioFilePath: String
) {
    val context = LocalContext.current
    var amplitudes by remember { mutableStateOf(listOf<Int>()) }
    LaunchedEffect(Unit) {
        amplitudes = AudioManager.getAmplitudes(context, audioFilePath)
    }
    if (amplitudes.isNotEmpty()) {
        Row(Modifier.fillMaxWidth()) {
            var windowOffset by remember { mutableStateOf(0F) }
            AudioWaveform(
                amplitudes = amplitudes,
                waveformBrush = SolidColor(Color.Red),
                windowOffset = windowOffset,
                onWindowSlide = { windowOffset = it }
            )
        }
    }
}
