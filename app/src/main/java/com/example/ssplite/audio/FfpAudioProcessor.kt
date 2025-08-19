package com.example.ssplite.audio

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioProcessor.AudioFormat
import androidx.media3.exoplayer.audio.BaseAudioProcessor
import com.example.ssplite.model.Preset
import java.nio.ByteBuffer

/**
 * AudioProcessor applying FFP-inspired EQ and simple dynamics.
 */
@UnstableApi
class FfpAudioProcessor : BaseAudioProcessor() {

    private val lowShelf = Biquad()
    private val highShelf = Biquad()
    private val peaks = mutableListOf<Biquad>()
    private val dynamics = Dynamics()
    private val lfo = Lfo()

    private var sampleRate = 0
    private var preset: Preset? = null
    private var bypass = false

    fun setPreset(p: Preset) {
        preset = p
        if (sampleRate != 0) applyPreset(p)
    }

    fun setBypass(enabled: Boolean) {
        bypass = enabled
    }

    fun setLfoEnabled(enabled: Boolean) { lfo.enabled = enabled }
    fun setLfoRateHz(rate: Float) { lfo.rateHz = rate }
    fun setLfoDepthDb(depth: Float) { lfo.depthDb = depth }

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        sampleRate = inputAudioFormat.sampleRate
        lfo.setSampleRate(sampleRate)
        preset?.let { applyPreset(it) }
        return inputAudioFormat
    }

    private fun applyPreset(p: Preset) {
        val rate = sampleRate.toDouble()
        lowShelf.setLowShelf(rate, p.eq.low_shelf.fc_hz.toDouble(), p.eq.low_shelf.gain_db.toDouble())
        highShelf.setHighShelf(rate, p.eq.high_shelf.fc_hz.toDouble(), p.eq.high_shelf.gain_db.toDouble())
        peaks.clear()
        p.eq.peaks.forEach { peak ->
            val b = Biquad()
            b.setPeaking(rate, peak.fc_hz.toDouble(), peak.q.toDouble(), peak.gain_db.toDouble())
            peaks.add(b)
        }
        dynamics.preGainDb = p.dynamics.pregain_db
        dynamics.ceilingDbfs = p.dynamics.limiter.ceiling_dbfs
        lfo.enabled = p.modulation.enabled
        lfo.rateHz = p.modulation.rate_hz
        lfo.depthDb = p.modulation.depth_db
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val out = replaceOutputBuffer(inputBuffer.remaining())
        while (inputBuffer.hasRemaining()) {
            val s = inputBuffer.float
            if (bypass) {
                out.putFloat(s)
            } else {
                var y = lowShelf.process(s)
                peaks.forEach { y = it.process(y) }
                y = highShelf.process(y)
                y = dynamics.process(y)
                y *= lfo.next()
                out.putFloat(y)
            }
        }
        out.flip()
    }

    override fun onFlush() {
        lowShelf.reset()
        peaks.forEach { it.reset() }
        highShelf.reset()
        dynamics.reset()
        lfo.reset()
    }

    override fun isEnded(): Boolean = false
}
