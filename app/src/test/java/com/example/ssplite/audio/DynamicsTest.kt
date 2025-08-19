package com.example.ssplite.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicsTest {
    @Test
    fun limiterPreventsClipping() {
        val dyn = Dynamics(preGainDb = 0f, ceilingDbfs = -6f, lookaheadMs = 0f, releaseMs = 100f)
        var out = 0f
        repeat(20) { out = dyn.process(1f) }
        assertTrue(out <= 0.51f)
    }

    @Test
    fun compressorReducesGain() {
        val dyn = Dynamics(preGainDb = 0f, ceilingDbfs = 0f, lookaheadMs = 0f, releaseMs = 100f)
        dyn.setCompressor(true, 2f, -20f, 1f, 100f)
        var out = 0f
        repeat(200) { out = dyn.process(1f) }
        assertTrue(out < 1f)
    }

    @Test
    fun resetClearsState() {
        val dyn = Dynamics()
        var out = dyn.process(1f)
        assertTrue(out != 0f)
        dyn.reset()
        out = dyn.process(0f)
        assertEquals(0f, out, 1e-6f)
    }
}

