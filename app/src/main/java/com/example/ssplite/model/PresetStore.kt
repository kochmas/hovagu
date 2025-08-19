package com.example.ssplite.model

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
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

    private fun dir(context: Context): File {
        val d = File(context.filesDir, "presets")
        if (!d.exists()) d.mkdirs()
        return d
    }

    fun list(context: Context): List<String> {
        return dir(context).listFiles()?.filter { it.extension == "json" }?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    fun save(context: Context, preset: Preset) {
        val file = File(dir(context), "${preset.name}.json")
        file.writeText(json.encodeToString(preset))
    }

    fun load(context: Context, name: String): Preset {
        val file = File(dir(context), "$name.json")
        return json.decodeFromString(file.readText())
    }

    fun import(context: Context, stream: InputStream): Preset {
        val preset = load(stream)
        save(context, preset)
        return preset
    }

    fun export(context: Context, name: String, stream: OutputStream) {
        val preset = load(context, name)
        save(stream, preset)
    }
}
