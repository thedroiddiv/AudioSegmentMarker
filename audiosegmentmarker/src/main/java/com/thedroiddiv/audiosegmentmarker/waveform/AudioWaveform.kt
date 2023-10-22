package com.thedroiddiv.audiosegmentmarker.waveform

import android.view.MotionEvent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.thedroiddiv.audiosegmentmarker.waveform.model.AmplitudeType
import com.thedroiddiv.audiosegmentmarker.waveform.model.WaveformAlignment
import kotlin.math.abs

private val MinSpikeWidthDp: Dp = 1.dp
private val MaxSpikeWidthDp: Dp = 24.dp
private val MinSpikePaddingDp: Dp = 0.dp
private val MaxSpikePaddingDp: Dp = 12.dp
private val MinSpikeRadiusDp: Dp = 0.dp
private val MaxSpikeRadiusDp: Dp = 12.dp

private const val MinSpikeHeight: Float = 1F

@Suppress("LocalVariableName")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    style: DrawStyle = Fill,
    waveformBrush: Brush = SolidColor(Color.White),
    waveformAlignment: WaveformAlignment = WaveformAlignment.Center,
    amplitudeType: AmplitudeType = AmplitudeType.Avg,
    spikeAnimationSpec: AnimationSpec<Float> = tween(500),
    spikeWidth: Dp = 4.dp,
    spikeRadius: Dp = 2.dp,
    spikePadding: Dp = 1.dp,
    amplitudes: List<Int>,
    windowSize: Float = 0.2F
) {
    assert(windowSize in 0F..1F)

    val _spikeWidth = remember(spikeWidth) { spikeWidth.coerceIn(MinSpikeWidthDp, MaxSpikeWidthDp) }
    val _spikePadding =
        remember(spikePadding) { spikePadding.coerceIn(MinSpikePaddingDp, MaxSpikePaddingDp) }
    val _spikeRadius =
        remember(spikeRadius) { spikeRadius.coerceIn(MinSpikeRadiusDp, MaxSpikeRadiusDp) }
    val _spikeTotalWidth = remember(spikeWidth, spikePadding) { _spikeWidth + _spikePadding }
    var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }
    var spikes by remember { mutableStateOf(0F) }
    val spikesAmplitudes = remember(amplitudes, spikes, amplitudeType) {
        amplitudes.toDrawableAmplitudes(
            amplitudeType = amplitudeType,
            spikes = spikes.toInt(),
            minHeight = MinSpikeHeight,
            maxHeight = canvasSize.height.coerceAtLeast(MinSpikeHeight)
        )
    }.map { animateFloatAsState(it, spikeAnimationSpec, label = "spikes amplitude").value }

    var windowOffset by remember { mutableStateOf(0F) }
    val _windowOffset by animateFloatAsState(windowOffset, label = "window offset")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(48.dp)
                .pointerInteropFilter {
                    return@pointerInteropFilter when (it.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            if (it.x in 0F..canvasSize.width) {
                                val maximumWindowOffset = canvasSize.width.times(1 - windowSize)
                                windowOffset = if (it.x in maximumWindowOffset..canvasSize.width) {
                                    (maximumWindowOffset / canvasSize.width)
                                } else {
                                    (it.x / canvasSize.width)
                                }
                                true
                            } else false
                        }

                        else -> false
                    }
                }
                .then(modifier)
        ) {
            canvasSize = size
            spikes = size.width / _spikeTotalWidth.toPx()
            spikesAmplitudes.forEachIndexed { index, amplitude ->
                drawRoundRect(
                    brush = waveformBrush,
                    topLeft = Offset(
                        x = index * _spikeTotalWidth.toPx(),
                        y = when (waveformAlignment) {
                            WaveformAlignment.Top -> 0F
                            WaveformAlignment.Bottom -> size.height - amplitude
                            WaveformAlignment.Center -> size.height / 2F - amplitude / 2F
                        }
                    ),
                    size = Size(
                        width = _spikeWidth.toPx(),
                        height = amplitude
                    ),
                    cornerRadius = CornerRadius(_spikeRadius.toPx(), _spikeRadius.toPx()),
                    style = style
                )
            }

            val windowWidth = canvasSize.width.times(windowSize)
            val windowHeight = canvasSize.height

            drawRoundRect(
                brush = SolidColor(Color.Gray.copy(alpha = 0.5f)),
                size = Size(
                    width = windowWidth,
                    height = windowHeight
                ),
                topLeft = Offset(
                    x = canvasSize.width.times(_windowOffset),
                    y = 0f
                )
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(48.dp)
        ) {

        }
    }
}

private fun List<Int>.toDrawableAmplitudes(
    amplitudeType: AmplitudeType,
    spikes: Int,
    minHeight: Float,
    maxHeight: Float
): List<Float> {
    val amplitudes = map(Int::toFloat)
    if (amplitudes.isEmpty() || spikes == 0) {
        return List(spikes) { minHeight }
    }
    val transform = { data: List<Float> ->
        when (amplitudeType) {
            AmplitudeType.Avg -> data.average()
            AmplitudeType.Max -> data.max()
            AmplitudeType.Min -> data.min()
        }.toFloat().coerceIn(minHeight, maxHeight)
    }
    return when {
        spikes > amplitudes.count() -> amplitudes.fillToSize(spikes, transform)
        else -> amplitudes.chunkToSize(spikes, transform)
    }.normalize(minHeight, maxHeight)
}