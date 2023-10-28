package com.daiatech.audiosegmentmarker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.daiatech.audiosegmentmarker.R
import com.daiatech.audiosegmentmarker.data.AudioManager
import com.daiatech.audiosegmentmarker.waveform.AudioWaveform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun AudioSegmentMarker(
    audioFilePath: String,
    markers: List<Float>,
    onMarkerAdd: (Float) -> Unit
) {
    val context = LocalContext.current
    var amplitudes by remember { mutableStateOf(listOf<Int>()) }
    var durationMs by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var progressMs by remember { mutableStateOf(0L) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPaused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val (amp, dur) = AudioManager.getAmplitudes(context, audioFilePath)
        amplitudes = amp
        durationMs = dur
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            private var timeoutJob: Job? = null
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_ENDED, ExoPlayer.STATE_IDLE -> {
                        timeoutJob?.cancel()
                        progressMs = 0
                    }

                    else -> {}
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                super.onIsPlayingChanged(playing)
                isPlaying = playing
                timeoutJob?.cancel()
                if (playing) {
                    isPaused = false
                    timeoutJob = scope.launch(Dispatchers.Main) {
                        while (isActive) {
                            delay(100)
                            progressMs += 100
                            // endMs?.let { if (exoPlayer.currentPosition >= it) exoPlayer.stop() }
                        }
                    }
                }
            }
        }

        exoPlayer.addListener(listener)

        // Cleanup when component is destroyed
        onDispose {
            exoPlayer.release()
        }
    }

    if (amplitudes.isNotEmpty()) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioPlayer(
                    isPlaying = isPlaying,
                    currentPosition = progressMs,
                    durationMS = durationMs,
                    onPlay = {
                        if (!isPaused) {
                            val mediaItem = MediaItem.fromUri(audioFilePath)
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                            // startMs?.let { exoPlayer.seekTo(it) }
                        }
                        exoPlayer.play()
                    },
                    onPause = {
                        exoPlayer.pause()
                        isPaused = true
                    }
                )
            }

            Row(Modifier.fillMaxWidth()) {
                AudioWaveform(
                    amplitudes = amplitudes,
                    waveformBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.border(2.dp, Color.Black),
                    markers = markers,
                    addMarker = onMarkerAdd,
                    progress = if (durationMs == 0L) 0f else progressMs.toFloat().div(durationMs)
                )
            }
        }
    }
}

@Composable
fun RowScope.AudioPlayer(
    isPlaying: Boolean,
    currentPosition: Long,
    durationMS: Long,
    onPlay: () -> Unit,
    onPause: () -> Unit,
) {
    val progress = if (durationMS == 0L) 0f else currentPosition.toFloat().div(durationMS)
    Image(
        painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
        contentDescription = "play",
        modifier = Modifier
            .size(48.dp)
            .clickable { if (isPlaying) onPause() else onPlay() }
    )
    LinearProgressIndicator(progress = progress, modifier = Modifier.weight(1f))
    Text(text = "\t${millisecondsToMmSs(currentPosition)}/${millisecondsToMmSs(durationMS)}\t")
}

fun millisecondsToMmSs(milliseconds: Long): String {
    // Calculate seconds and minutes from milliseconds
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60

    // Format minutes and seconds as strings in "mm:ss" format
    val formattedSeconds = seconds.toString().padStart(2, '0')

    // Combine minutes and seconds in the "mm:ss" format
    return "$minutes:$formattedSeconds"
}