package com.example.ssplite.ui

import android.content.Context
import android.content.Intent
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.example.ssplite.audio.AudioEngine
import com.example.ssplite.audio.FfpAudioProcessor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavController

private val SUPPORTED_AUDIO_EXTENSIONS = setOf(
    "mp3", "wav", "flac", "ogg", "m4a", "aac"
)

private fun isSupportedAudio(file: DocumentFile): Boolean {
    val name = file.name?.lowercase() ?: return false
    return SUPPORTED_AUDIO_EXTENSIONS.any { ext -> name.endsWith(".$ext") }
}

private fun DocumentFile.findFileByRelativePath(path: String): DocumentFile? {
    val parts = path.split("/").filter { it.isNotEmpty() }
    var current: DocumentFile? = this
    for (part in parts) {
        current = current?.listFiles()?.firstOrNull { it.name == part }
        if (current == null) return null
    }
    return current
}

private fun buildPlaylist(root: DocumentFile?, context: Context): List<Uri> {
    if (root == null) return emptyList()

    fun findPlaylistFile(dir: DocumentFile): DocumentFile? {
        dir.listFiles().forEach { file ->
            if (file.isFile) {
                val name = file.name?.lowercase()
                if (name != null && (name.endsWith(".m3u") || name.endsWith(".m3u8"))) {
                    return file
                }
            } else if (file.isDirectory) {
                findPlaylistFile(file)?.let { return it }
            }
        }
        return null
    }

    val playlistFile = findPlaylistFile(root)
    return if (playlistFile != null) {
        val items = mutableListOf<Uri>()
        context.contentResolver.openInputStream(playlistFile.uri)?.bufferedReader()?.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    val track = root.findFileByRelativePath(trimmed)
                    if (track != null && track.isFile && isSupportedAudio(track)) {
                        items.add(track.uri)
                    }
                }
            }
        }
        items
    } else {
        val uris = mutableListOf<Uri>()
        fun traverse(dir: DocumentFile) {
            dir.listFiles().forEach { file ->
                if (file.isDirectory) traverse(file)
                else if (file.isFile && isSupportedAudio(file)) uris.add(file.uri)
            }
        }
        traverse(root)
        uris
    }
}

@Composable
fun PlayerScreen(navController: NavController, testPlayer: ExoPlayer? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var treeUri by remember { mutableStateOf<Uri?>(null) }
    var playlist by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var sessionMinutes by remember { mutableStateOf(0) }
    var trackPositionMs by remember { mutableStateOf(0L) }
    var sessionElapsedMs by remember { mutableStateOf(0L) }
    var countdownMs by remember { mutableStateOf(0L) }
    var targetStopIndex by remember { mutableStateOf<Int?>(null) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }

    val processor = remember {
        AudioEngine.processor ?: FfpAudioProcessor().also { AudioEngine.processor = it }
    }

    @OptIn(UnstableApi::class)
    val player = testPlayer ?: remember {
        AudioEngine.player ?: run {
            val audioSink = DefaultAudioSink.Builder()
                .setAudioProcessors(processor)
                .build()
            ExoPlayer.Builder(context)
                .setAudioSink(audioSink)
                .build()
                .also { AudioEngine.player = it }
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
            if (AudioEngine.player == player) {
                AudioEngine.player = null
            }
        }
    }

    val openTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        treeUri = uri
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val docs = DocumentFile.fromTreeUri(context, uri)
            playlist = buildPlaylist(docs, context)
            player.setMediaItems(playlist.map { MediaItem.fromUri(it) })
            player.prepare()
        }
    }

    LaunchedEffect(player) {
        while (true) {
            trackPositionMs = player.currentPosition
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
                    sessionElapsedMs = 0L
                    targetStopIndex = null
                    countdownJob = scope.launch {
                        var remaining = countdownMs
                        while (remaining > 0) {
                            if (player.isPlaying) {
                                delay(1000)
                                remaining -= 1000
                                sessionElapsedMs += 1000
                                countdownMs = remaining
                                if (targetStopIndex == null) {
                                    val remainingTrack = player.duration - player.currentPosition
                                    if (remaining <= remainingTrack) {
                                        targetStopIndex = player.currentMediaItemIndex
                                        val stopIdx = targetStopIndex!!
                                        if (player.mediaItemCount > stopIdx + 1) {
                                            player.removeMediaItems(stopIdx + 1, player.mediaItemCount)
                                        }
                                    }
                                }
                            } else {
                                delay(200)
                            }
                        }
                        if (targetStopIndex == null) {
                            targetStopIndex = player.currentMediaItemIndex
                            val stopIdx = targetStopIndex!!
                            if (player.mediaItemCount > stopIdx + 1) {
                                player.removeMediaItems(stopIdx + 1, player.mediaItemCount)
                            }
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
        if (targetStopIndex != null) {
            Text("Geplantes Ende nach Track ${'$'}{targetStopIndex!! + 1}")
            Spacer(Modifier.height(8.dp))
        }
        Text("Track: ${'$'}{player.currentMediaItemIndex + 1}/${'$'}{playlist.size}")
        LinearProgressIndicator(
            progress = if (player.duration > 0) trackPositionMs / player.duration.toFloat() else 0f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
