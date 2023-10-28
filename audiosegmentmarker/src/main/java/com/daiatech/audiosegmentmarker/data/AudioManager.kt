package com.daiatech.audiosegmentmarker.data

import android.content.Context
import android.util.Log
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaResult

object AudioManager {
    private lateinit var amplituda: Amplituda
    fun getAmplitudes(context: Context, audioFilePath: String): Pair<List<Int>, Long> {
        if (!::amplituda.isInitialized) {
            amplituda = Amplituda(context)
        }
        var amplitudes = listOf<Int>()
        var duration = 0L
        amplituda.processAudio(audioFilePath)
            .get({
                amplitudes = it.amplitudesAsList()
                duration = it.getAudioDuration(AmplitudaResult.DurationUnit.MILLIS)
            }, { e ->
                Log.e("AudioManager::", "getAmplitudes: ", e)
            })
        return Pair(amplitudes, duration)
    }
}