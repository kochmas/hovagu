package com.example.ssplite.audio

import kotlin.math.*

/**
 * Simple biquad IIR filter supporting peaking and shelving modes.
 */
class Biquad {
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0

    private var in1 = 0.0
    private var in2 = 0.0
    private var out1 = 0.0
    private var out2 = 0.0

    fun reset() {
        in1 = 0.0
        in2 = 0.0
        out1 = 0.0
        out2 = 0.0
    }

    fun process(x: Float): Float {
        val y = b0 * x + b1 * in1 + b2 * in2 - a1 * out1 - a2 * out2
        in2 = in1
        in1 = x.toDouble()
        out2 = out1
        out1 = y
        return y.toFloat()
    }

    fun setPeaking(fs: Double, f0: Double, q: Double, gainDb: Double) {
        val A = 10.0.pow(gainDb / 40.0)
        val w0 = 2 * Math.PI * f0 / fs
        val alpha = sin(w0) / (2 * q)
        val cosw0 = cos(w0)

        val b0 = 1 + alpha * A
        val b1 = -2 * cosw0
        val b2 = 1 - alpha * A
        val a0 = 1 + alpha / A
        val a1 = -2 * cosw0
        val a2 = 1 - alpha / A

        this.b0 = b0 / a0
        this.b1 = b1 / a0
        this.b2 = b2 / a0
        this.a1 = a1 / a0
        this.a2 = a2 / a0
    }

    fun setLowShelf(fs: Double, f0: Double, gainDb: Double) {
        val A = 10.0.pow(gainDb / 40.0)
        val w0 = 2 * Math.PI * f0 / fs
        val cosw0 = cos(w0)
        val sinw0 = sin(w0)
        val alpha = sinw0 / 2 * sqrt((A + 1 / A) * (1 / 0.707 - 1) + 2.0)
        val beta = 2 * sqrt(A) * alpha
        val a0 = (A + 1) + (A - 1) * cosw0 + beta
        b0 = A * ((A + 1) - (A - 1) * cosw0 + beta) / a0
        b1 = 2 * A * ((A - 1) - (A + 1) * cosw0) / a0
        b2 = A * ((A + 1) - (A - 1) * cosw0 - beta) / a0
        a1 = -2 * ((A - 1) + (A + 1) * cosw0) / a0
        a2 = ((A + 1) + (A - 1) * cosw0 - beta) / a0
    }

    fun setHighShelf(fs: Double, f0: Double, gainDb: Double) {
        val A = 10.0.pow(gainDb / 40.0)
        val w0 = 2 * Math.PI * f0 / fs
        val cosw0 = cos(w0)
        val sinw0 = sin(w0)
        val alpha = sinw0 / 2 * sqrt((A + 1 / A) * (1 / 0.707 - 1) + 2.0)
        val beta = 2 * sqrt(A) * alpha
        val a0 = (A + 1) - (A - 1) * cosw0 + beta
        b0 = A * ((A + 1) + (A - 1) * cosw0 + beta) / a0
        b1 = -2 * A * ((A - 1) + (A + 1) * cosw0) / a0
        b2 = A * ((A + 1) + (A - 1) * cosw0 - beta) / a0
        a1 = 2 * ((A - 1) - (A + 1) * cosw0) / a0
        a2 = ((A + 1) - (A - 1) * cosw0 - beta) / a0
    }
}
