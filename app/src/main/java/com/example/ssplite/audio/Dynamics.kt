package com.example.ssplite.audio

import kotlin.math.pow
import kotlin.math.sign

/** Simple pre-gain and hard limiter. */
class Dynamics(
    var preGainDb: Float = -3f,
    var ceilingDbfs: Float = -1f
) {
    private fun preGain(): Float = 10f.pow(preGainDb / 20f)
    private fun ceiling(): Float = 10f.pow(ceilingDbfs / 20f)

    fun process(sample: Float): Float {
        val x = sample * preGain()
        val c = ceiling()
        return when {
            x > c -> c
            x < -c -> -c
            else -> x
        }
    }

    fun reset() {}
}
