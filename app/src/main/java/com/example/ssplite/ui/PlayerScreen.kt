package com.example.ssplite.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.ssplite.audio.FfpAudioProcessor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@Composable
fun PlayerScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var treeUri by remember { mutableStateOf<Uri?>(null) }
    var playlist by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var sessionMinutes by remember { mutableStateOf(0) }
    var elapsedMs by remember { mutableStateOf(0L) }
    var countdownMs by remember { mutableStateOf(0L) }
    var targetStopIndex by remember { mutableStateOf<Int?>(null) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }

    val player = remember {
        ExoPlayer.Builder(context)
            .setAudioProcessors(listOf(FfpAudioProcessor()))
            .build()
    }

    val openTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        treeUri = uri
        if (uri != null) {
            val docs = DocumentFile.fromTreeUri(context, uri)
            playlist = docs?.listFiles()?.filter { it.isFile }?.map { it.uri } ?: emptyList()
            player.setMediaItems(playlist.map { MediaItem.fromUri(it) })
            player.prepare()
        }
    }

    LaunchedEffect(player) {
        while (true) {
            elapsedMs = player.currentPosition
            delay(1000)
        }
    }

    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { openTree.launch(null) }) { Text("Ordner wählen") }
            Button(onClick = { navController.navigate("filter") }) { Text("Filter") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = if (sessionMinutes == 0) "" else sessionMinutes.toString(),
            onValueChange = { sessionMinutes = it.toIntOrNull() ?: 0 },
            label = { Text("Session (Minuten)") }
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = {
                player.play()
                countdownJob?.cancel()
                if (sessionMinutes > 0) {
                    countdownMs = sessionMinutes * 60_000L
                    targetStopIndex = null
                    countdownJob = scope.launch {
                        var remaining = countdownMs
                        while (remaining > 0) {
                            if (player.isPlaying) {
                                delay(1000)
                                remaining -= 1000
                                countdownMs = remaining
                            } else {
                                delay(200)
                            }
                        }
                        targetStopIndex = player.currentMediaItemIndex
                        val stopIdx = targetStopIndex!!
                        if (player.mediaItemCount > stopIdx + 1) {
                            player.removeMediaItems(stopIdx + 1, player.mediaItemCount)
                        }
                    }
                }
            }) { Text("Play") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { player.pause() }) { Text("Pause") }
        }
        Spacer(Modifier.height(8.dp))
        if (sessionMinutes > 0) {
            val minutes = countdownMs / 60_000
            val seconds = (countdownMs / 1000) % 60
            Text(String.format("Restzeit: %02d:%02d", minutes, seconds))
            Spacer(Modifier.height(8.dp))
        }
        Text("Track: ${'$'}{player.currentMediaItemIndex + 1}/${'$'}{playlist.size}")
        LinearProgressIndicator(
            progress = if (player.duration > 0) elapsedMs / player.duration.toFloat() else 0f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
