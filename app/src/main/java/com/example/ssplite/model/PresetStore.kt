package com.example.ssplite.model

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Utility for saving and loading presets as JSON. */
object PresetStore {
    private val json = Json { prettyPrint = true }

    fun save(stream: OutputStream, preset: Preset) {
        stream.writer().use { it.write(json.encodeToString(preset)) }
    }

    fun load(stream: InputStream): Preset {
        return json.decodeFromString(stream.reader().readText())
    }
}
