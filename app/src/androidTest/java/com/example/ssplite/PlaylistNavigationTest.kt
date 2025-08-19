package com.example.ssplite

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.FakeExoPlayer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaylistNavigationTest {
    @Test
    fun advancesThroughPlaylist() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val player = FakeExoPlayer.Builder(context).build()
        val items = listOf(
            MediaItem.fromUri("asset:///one"),
            MediaItem.fromUri("asset:///two")
        )
        player.setMediaItems(items)
        player.prepare()
        assertEquals(0, player.currentMediaItemIndex)
        player.seekToNextMediaItem()
        assertEquals(1, player.currentMediaItemIndex)
    }
}

