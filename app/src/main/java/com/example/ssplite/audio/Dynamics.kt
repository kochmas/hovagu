package com.example.ssplite.audio

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

/**
 * Pre-gain, optional compression and look-ahead limiting.
 *
 * The limiter uses a simple buffer based look-ahead design.  A peak is searched
 * within the current look-ahead window and gain is adjusted to keep the signal
 * below the configured ceiling.  Gain recovers according to the configured
 * release time.  A mild feed forward compressor may be enabled which applies a
 * single pole envelope detector.
 */
class Dynamics(
    var preGainDb: Float = -3f,
    var ceilingDbfs: Float = -1f,
    var lookaheadMs: Float = 0f,
    var releaseMs: Float = 100f
) {

    // Sample rate dependent state
    private var sampleRate: Int = 48_000
    private var lookaheadSamples = 1
    private var buffer = FloatArray(lookaheadSamples)
    private var index = 0
    private var limiterGain = 1f
    private var limiterReleaseCoef = 0f

    // Compressor configuration
    private var compEnabled = false
    private var compRatio = 1.3f
    private var compThresholdDbfs = -24f
    private var compAttackMs = 15f
    private var compReleaseMs = 120f

    // Compressor state
    private var compEnv = 0f
    private var compGain = 1f
    private var compThreshold = 0f
    private var compAttackCoef = 0f
    private var compReleaseCoef = 0f

    init {
        updateLimiterCoeffs()
        updateCompressorCoeffs()
    }

    fun setSampleRate(sr: Int) {
        sampleRate = sr
        updateLimiterCoeffs()
        updateCompressorCoeffs()
    }

    fun setLookaheadMs(ms: Float) {
        lookaheadMs = ms
        updateLimiterCoeffs()
    }

    fun setReleaseMs(ms: Float) {
        releaseMs = ms
        updateLimiterCoeffs()
    }

    fun setCompressor(
        enabled: Boolean,
        ratio: Float,
        thresholdDbfs: Float,
        attackMs: Float,
        releaseMs: Float
    ) {
        compEnabled = enabled
        compRatio = ratio
        compThresholdDbfs = thresholdDbfs
        compAttackMs = attackMs
        compReleaseMs = releaseMs
        updateCompressorCoeffs()
    }

    private fun preGain(): Float = 10f.pow(preGainDb / 20f)
    private fun ceiling(): Float = 10f.pow(ceilingDbfs / 20f)

    private fun updateLimiterCoeffs() {
        lookaheadSamples = (lookaheadMs / 1000f * sampleRate).toInt().coerceAtLeast(1)
        if (buffer.size != lookaheadSamples) {
            buffer = FloatArray(lookaheadSamples)
            index = 0
        }
        limiterReleaseCoef = exp(-1f / (releaseMs / 1000f * sampleRate))
    }

    private fun updateCompressorCoeffs() {
        compThreshold = 10f.pow(compThresholdDbfs / 20f)
        compAttackCoef = exp(-1f / (compAttackMs / 1000f * sampleRate))
        compReleaseCoef = exp(-1f / (compReleaseMs / 1000f * sampleRate))
    }

    fun process(sample: Float): Float {
        var x = sample * preGain()

        if (compEnabled) {
            val inp = abs(x)
            compEnv = if (inp > compEnv) {
                compAttackCoef * (compEnv - inp) + inp
            } else {
                compReleaseCoef * (compEnv - inp) + inp
            }
            val desired = if (compEnv <= compThreshold || compThreshold == 0f) {
                1f
            } else {
                (compThreshold + (compEnv - compThreshold) / compRatio) / compEnv
            }
            compGain = if (desired < compGain) {
                desired
            } else {
                compGain + (desired - compGain) * (1 - compReleaseCoef)
            }
            x *= compGain
        }

        // Limiter: push sample into buffer and compute max peak
        buffer[index] = x
        var max = 0f
        for (s in buffer) {
            val a = abs(s)
            if (a > max) max = a
        }
        val c = ceiling()
        val desiredGain = if (max <= c) 1f else c / max
        limiterGain = if (desiredGain < limiterGain) {
            desiredGain
        } else {
            limiterGain + (desiredGain - limiterGain) * (1 - limiterReleaseCoef)
        }
        val out = buffer[(index + 1) % buffer.size] * limiterGain
        index = (index + 1) % buffer.size
        return out
    }

    fun reset() {
        buffer.fill(0f)
        index = 0
        limiterGain = 1f
        compEnv = 0f
        compGain = 1f
    }
}

