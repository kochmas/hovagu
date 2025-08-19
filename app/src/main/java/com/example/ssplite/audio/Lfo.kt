package com.example.ssplite.audio

import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.pow

/** Simple sine LFO returning a linear gain factor. */
class Lfo(
    var enabled: Boolean = false,
    var rateHz: Float = 0.5f,
    var depthDb: Float = 0f
) {
    private var phase = 0.0
    private var sampleRate = 44100

    fun setSampleRate(rate: Int) {
        sampleRate = rate
    }

    fun reset() {
        phase = 0.0
    }

    fun next(): Float {
        if (!enabled) return 1f
        val value = sin(2 * PI * phase).toFloat()
        phase += rateHz / sampleRate
        if (phase >= 1.0) phase -= 1.0
        val db = value * depthDb
        return 10f.pow(db / 20f)
    }
}
