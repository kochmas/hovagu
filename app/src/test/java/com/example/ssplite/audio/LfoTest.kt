package com.example.ssplite.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.pow

class LfoTest {
    @Test
    fun producesSineGainWhenEnabled() {
        val lfo = Lfo(enabled = true, rateHz = 1f, depthDb = 6f)
        lfo.setSampleRate(4)
        val expected = listOf(1f, 10f.pow(6f / 20f), 1f, 10f.pow(-6f / 20f))
        expected.forEach { exp ->
            val actual = lfo.next()
            assertEquals(exp, actual, 1e-4f)
        }
    }

    @Test
    fun returnsUnityWhenDisabled() {
        val lfo = Lfo(enabled = false)
        repeat(3) { assertEquals(1f, lfo.next(), 0f) }
    }
}
