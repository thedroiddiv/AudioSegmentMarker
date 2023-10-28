package com.daiatech.audiosegmentmarker.data

import android.content.Context
import android.util.Log
import linc.com.amplituda.Amplituda

object AudioManager {
    private lateinit var amplituda: Amplituda
    fun getAmplitudes(context: Context, audioFilePath: String): List<Int> {
        if (!::amplituda.isInitialized) {
            amplituda = Amplituda(context)
        }
        var amplitudes = listOf<Int>()
        amplituda.processAudio(audioFilePath)
            .get({
                amplitudes = it.amplitudesAsList()
            }, { e ->
                Log.e("AudioManager::", "getAmplitudes: ", e)
            })
        return amplitudes
    }
}