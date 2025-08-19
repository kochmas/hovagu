package com.example.ssplite.audio

import kotlin.test.Test
import kotlin.test.assertEquals

class BiquadTest {
    @Test
    fun peakingWithZeroGainActsAsBypass() {
        val biquad = Biquad()
        biquad.setPeaking(fs = 48_000.0, f0 = 1_000.0, q = 1.0, gainDb = 0.0)
        val input = floatArrayOf(0.5f, -0.25f, 0.125f)
        input.forEach { sample ->
            val out = biquad.process(sample)
            assertEquals(sample, out, 1e-3f)
        }
    }
}
