package com.example.ssplite.audio

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioProcessor.AudioFormat
import androidx.media3.exoplayer.audio.BaseAudioProcessor
import java.nio.ByteBuffer

/**
 * AudioProcessor applying FFP-inspired EQ and simple dynamics.
 */
@UnstableApi
class FfpAudioProcessor : BaseAudioProcessor() {

    private val lowShelf = Biquad()
    private val mid1 = Biquad()
    private val mid2 = Biquad()
    private val highShelf = Biquad()
    private val dynamics = Dynamics()

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        val rate = inputAudioFormat.sampleRate
        lowShelf.setLowShelf(rate.toDouble(), 150.0, -10.0)
        mid1.setPeaking(rate.toDouble(), 1000.0, 1.0, 4.0)
        mid2.setPeaking(rate.toDouble(), 2000.0, 1.0, 4.0)
        highShelf.setHighShelf(rate.toDouble(), 5000.0, -8.0)
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val out = replaceOutputBuffer(inputBuffer.remaining())
        while (inputBuffer.hasRemaining()) {
            val s = inputBuffer.float
            var y = lowShelf.process(s)
            y = mid1.process(y)
            y = mid2.process(y)
            y = highShelf.process(y)
            y = dynamics.process(y)
            out.putFloat(y)
        }
        out.flip()
    }

    override fun onFlush() {
        lowShelf.reset()
        mid1.reset()
        mid2.reset()
        highShelf.reset()
        dynamics.reset()
    }

    override fun isEnded(): Boolean = false
}
