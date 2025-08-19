package com.example.ssplite.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ssplite.R
import com.example.ssplite.model.Preset
import com.example.ssplite.model.PresetStore
import com.example.ssplite.audio.AudioEngine

data class SystemPreset(val name: String, @RawRes val resId: Int)

@Composable
fun FilterScreen(navController: NavController) {
    val context = LocalContext.current
    val processor = AudioEngine.processor

    val systemPresets = listOf(
        SystemPreset("Soziale Stimme", R.raw.social_voice),
        SystemPreset("Kinderstimme", R.raw.kinderstimme),
        SystemPreset("Männerstimme", R.raw.maennerstimme)
    )

    var userPresets by remember { mutableStateOf(PresetStore.list(context)) }
    var currentPreset by remember { mutableStateOf<Preset?>(null) }
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    var presetMenuExpanded by remember { mutableStateOf(false) }
    var bypass by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                currentPreset = PresetStore.import(context, stream)
                selectedPreset = currentPreset?.name
                userPresets = PresetStore.list(context)
                processor?.setPreset(currentPreset!!)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            currentPreset?.let { preset ->
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    PresetStore.save(stream, preset)
                }
            }
        }
    }

    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box {
                Button(onClick = { presetMenuExpanded = true }) {
                    Text(selectedPreset ?: "Preset wählen")
                }
                DropdownMenu(expanded = presetMenuExpanded, onDismissRequest = { presetMenuExpanded = false }) {
                    systemPresets.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset.name) },
                            onClick = {
                                presetMenuExpanded = false
                                context.resources.openRawResource(preset.resId).use {
                                    currentPreset = PresetStore.load(it)
                                }
                                selectedPreset = preset.name
                                processor?.setPreset(currentPreset!!)
                            }
                        )
                    }
                    userPresets.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                presetMenuExpanded = false
                                currentPreset = PresetStore.load(context, name)
                                selectedPreset = name
                                processor?.setPreset(currentPreset!!)
                            }
                        )
                    }
                }
            }
            Row {
                Button(onClick = {
                    currentPreset?.let {
                        PresetStore.save(context, it)
                        userPresets = PresetStore.list(context)
                    }
                }) { Text("Save") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row {
            Button(onClick = { importLauncher.launch(arrayOf("application/json")) }) { Text("Import") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                currentPreset?.let { exportLauncher.launch("${it.name}.json") }
            }) { Text("Export") }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bypass")
            Spacer(Modifier.width(8.dp))
            Switch(checked = bypass, onCheckedChange = {
                bypass = it
                processor?.setBypass(it)
            })
        }

        Spacer(Modifier.height(16.dp))

        currentPreset?.let { preset ->
            OutlinedTextField(
                value = preset.name,
                onValueChange = { name -> currentPreset = preset.copy(name = name) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text("Low Shelf Gain: ${String.format("%.1f", preset.eq.low_shelf.gain_db)} dB")
            Slider(
                value = preset.eq.low_shelf.gain_db,
                onValueChange = { gain ->
                    val updated = preset.copy(
                        eq = preset.eq.copy(
                            low_shelf = preset.eq.low_shelf.copy(gain_db = gain)
                        )
                    )
                    currentPreset = updated
                    processor?.setPreset(updated)
                },
                valueRange = -12f..12f
            )

            Spacer(Modifier.height(8.dp))

            Text("High Shelf Gain: ${String.format("%.1f", preset.eq.high_shelf.gain_db)} dB")
            Slider(
                value = preset.eq.high_shelf.gain_db,
                onValueChange = { gain ->
                    val updated = preset.copy(
                        eq = preset.eq.copy(
                            high_shelf = preset.eq.high_shelf.copy(gain_db = gain)
                        )
                    )
                    currentPreset = updated
                    processor?.setPreset(updated)
                },
                valueRange = -12f..12f
            )

            Spacer(Modifier.height(8.dp))

            preset.eq.peaks.forEachIndexed { index, peak ->
                Text("Peak ${index + 1} Gain: ${String.format("%.1f", peak.gain_db)} dB")
                Slider(
                    value = peak.gain_db,
                    onValueChange = { gain ->
                        val peaks = preset.eq.peaks.toMutableList()
                        peaks[index] = peaks[index].copy(gain_db = gain)
                        val updated = preset.copy(eq = preset.eq.copy(peaks = peaks))
                        currentPreset = updated
                        processor?.setPreset(updated)
                    },
                    valueRange = -12f..12f
                )
                Spacer(Modifier.height(4.dp))
                Text("Peak ${index + 1} Q: ${String.format("%.2f", peak.q)}")
                Slider(
                    value = peak.q,
                    onValueChange = { q ->
                        val peaks = preset.eq.peaks.toMutableList()
                        peaks[index] = peaks[index].copy(q = q)
                        val updated = preset.copy(eq = preset.eq.copy(peaks = peaks))
                        currentPreset = updated
                        processor?.setPreset(updated)
                    },
                    valueRange = 0.1f..10f
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Modulation")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = preset.modulation.enabled,
                    onCheckedChange = { enabled ->
                        val updated = preset.copy(
                            modulation = preset.modulation.copy(enabled = enabled)
                        )
                        currentPreset = updated
                        processor?.setLfoEnabled(enabled)
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            Text("Modulation Rate: ${String.format("%.1f", preset.modulation.rate_hz)} Hz")
            Slider(
                value = preset.modulation.rate_hz,
                onValueChange = { rate ->
                    val updated = preset.copy(
                        modulation = preset.modulation.copy(rate_hz = rate)
                    )
                    currentPreset = updated
                    processor?.setLfoRateHz(rate)
                },
                valueRange = 0f..10f
            )

            Spacer(Modifier.height(8.dp))

            Text("Modulation Depth: ${String.format("%.1f", preset.modulation.depth_db)} dB")
            Slider(
                value = preset.modulation.depth_db,
                onValueChange = { depth ->
                    val updated = preset.copy(
                        modulation = preset.modulation.copy(depth_db = depth)
                    )
                    currentPreset = updated
                    processor?.setLfoDepthDb(depth)
                },
                valueRange = 0f..6f
            )

            Spacer(Modifier.height(8.dp))

            Text("Pregain: ${String.format("%.1f", preset.dynamics.pregain_db)} dB")
            Slider(
                value = preset.dynamics.pregain_db,
                onValueChange = { pg ->
                    val updated = preset.copy(
                        dynamics = preset.dynamics.copy(pregain_db = pg)
                    )
                    currentPreset = updated
                    processor?.setPreset(updated)
                },
                valueRange = 0f..24f
            )

            Spacer(Modifier.height(8.dp))

            Text("Limiter Ceiling: ${String.format("%.1f", preset.dynamics.limiter.ceiling_dbfs)} dBFS")
            Slider(
                value = preset.dynamics.limiter.ceiling_dbfs,
                onValueChange = { ceiling ->
                    val updated = preset.copy(
                        dynamics = preset.dynamics.copy(
                            limiter = preset.dynamics.limiter.copy(ceiling_dbfs = ceiling)
                        )
                    )
                    currentPreset = updated
                    processor?.setPreset(updated)
                },
                valueRange = -40f..0f
            )
        }
    }
}

