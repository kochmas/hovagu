package com.example.ssplite.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BiquadTest {
    @Test
    fun peakingZeroGainIsTransparent() {
        val biquad = Biquad()
        biquad.setPeaking(48_000.0, 1_000.0, 1.0, 0.0)
        val first = biquad.process(1f)
        assertEquals(1f, first, 1e-6f)
        val second = biquad.process(0f)
        assertEquals(0f, second, 1e-6f)
    }

    @Test
    fun resetClearsState() {
        val biquad = Biquad()
        biquad.setPeaking(48_000.0, 1_000.0, 1.0, 6.0)
        val out = biquad.process(1f)
        assertNotEquals(0f, out)
        biquad.reset()
        val afterReset = biquad.process(0f)
        assertEquals(0f, afterReset, 1e-6f)
    }
}

