package com.example.ssplite.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ssplite.R
import com.example.ssplite.model.Preset
import com.example.ssplite.model.PresetStore

data class SystemPreset(val name: String, @RawRes val resId: Int)

@Composable
fun FilterScreen(navController: NavController) {
    val context = LocalContext.current

    val systemPresets = listOf(
        SystemPreset("Soziale Stimme", R.raw.social_voice),
        SystemPreset("Kinderstimme", R.raw.kinderstimme),
        SystemPreset("Männerstimme", R.raw.maennerstimme)
    )

    var currentPreset by remember { mutableStateOf<Preset?>(null) }
    var selectedPreset by remember { mutableStateOf<SystemPreset?>(null) }
    var presetMenuExpanded by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                currentPreset = PresetStore.load(stream)
                selectedPreset = null
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
                    Text(selectedPreset?.name ?: "System-Preset")
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
                                selectedPreset = preset
                            }
                        )
                    }
                }
            }
            Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
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

        currentPreset?.let { preset ->
            OutlinedTextField(
                value = preset.name,
                onValueChange = { name -> currentPreset = preset.copy(name = name) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = preset.eq.low_shelf.gain_db.toString(),
                onValueChange = { text ->
                    text.toFloatOrNull()?.let { gain ->
                        currentPreset = preset.copy(
                            eq = preset.eq.copy(
                                low_shelf = preset.eq.low_shelf.copy(gain_db = gain)
                            )
                        )
                    }
                },
                label = { Text("Low Shelf Gain (dB)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = preset.eq.high_shelf.gain_db.toString(),
                onValueChange = { text ->
                    text.toFloatOrNull()?.let { gain ->
                        currentPreset = preset.copy(
                            eq = preset.eq.copy(
                                high_shelf = preset.eq.high_shelf.copy(gain_db = gain)
                            )
                        )
                    }
                },
                label = { Text("High Shelf Gain (dB)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = preset.modulation.rate_hz.toString(),
                onValueChange = { text ->
                    text.toFloatOrNull()?.let { rate ->
                        currentPreset = preset.copy(
                            modulation = preset.modulation.copy(rate_hz = rate)
                        )
                    }
                },
                label = { Text("Modulation Rate (Hz)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = preset.dynamics.pregain_db.toString(),
                onValueChange = { text ->
                    text.toFloatOrNull()?.let { pg ->
                        currentPreset = preset.copy(
                            dynamics = preset.dynamics.copy(pregain_db = pg)
                        )
                    }
                },
                label = { Text("Pregain (dB)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

