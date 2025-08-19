package com.example.ssplite.audio

import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DynamicsTest {

    @Test
    fun lookAheadLimiterApplied() {
        val sr = 48_000
        val dyn = Dynamics(preGainDb = 6f, ceilingDbfs = -1f, lookaheadMs = 5f, releaseMs = 100f)
        dyn.setSampleRate(sr)
        val la = (5f / 1000f * sr).toInt().coerceAtLeast(1)

        repeat(la) { dyn.process(0f) } // prime delay line
        dyn.process(1f) // loud sample enters buffer
        var out = 0f
        repeat(la) { out = dyn.process(0f) } // read delayed sample

        val ceiling = 10f.pow(-1f / 20f)
        assertEquals(ceiling, out, 1e-6f)
    }

    @Test
    fun compressorReducesGain() {
        val sr = 48_000
        val dyn = Dynamics(preGainDb = 0f, ceilingDbfs = 0f)
        dyn.setSampleRate(sr)
        dyn.setCompressor(true, 2f, -20f, 5f, 50f)
        val threshold = 10f.pow(-20f / 20f)
        val input = threshold * 2f
        val out = dyn.process(input)
        assertTrue(out < input)
    }
}

