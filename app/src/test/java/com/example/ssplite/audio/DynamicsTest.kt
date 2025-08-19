package com.example.ssplite.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.pow

class DynamicsTest {
    @Test
    fun preGainAndLimiterApplied() {
        val dyn = Dynamics(preGainDb = 6f, ceilingDbfs = -1f)
        val ceiling = 10f.pow(-1f / 20f)
        val input = 1f
        val out = dyn.process(input)
        assertEquals(ceiling, out, 1e-6f)
    }
}
