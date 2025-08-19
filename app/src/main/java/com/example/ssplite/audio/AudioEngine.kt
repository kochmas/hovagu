package com.example.ssplite.audio

import androidx.media3.exoplayer.ExoPlayer

/**
 * Holds references to the shared audio processor and player so UI screens can
 * apply changes to the currently running pipeline.
 */
object AudioEngine {
    var processor: FfpAudioProcessor? = null
    var player: ExoPlayer? = null
}

