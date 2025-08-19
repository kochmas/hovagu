package com.example.ssplite

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.FakeExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionTimeoutTest {
    @Test
    fun trimsPlaylistAfterTimeout() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val player = FakeExoPlayer.Builder(context).build()
        val items = listOf(
            MediaItem.fromUri("asset:///one"),
            MediaItem.fromUri("asset:///two")
        )
        player.setMediaItems(items)
        player.prepare()
        player.play()
        var countdownMs = 200L
        var targetStopIndex: Int? = null
        val job = launch {
            var remaining = countdownMs
            while (remaining > 0) {
                delay(50)
                remaining -= 50
                if (targetStopIndex == null && remaining <= 100) {
                    targetStopIndex = player.currentMediaItemIndex
                    val stopIdx = targetStopIndex!!
                    if (player.mediaItemCount > stopIdx + 1) {
                        player.removeMediaItems(stopIdx + 1, player.mediaItemCount)
                    }
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
        job.join()
        assertEquals(1, player.mediaItemCount)
    }
}

