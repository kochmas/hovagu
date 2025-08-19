package com.example.ssplite.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.media3.test.utils.FakeExoPlayer
import androidx.media3.test.utils.FakeMediaSource
import androidx.media3.test.utils.FakeTimeline
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionStopTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test
    fun sessionCountdownReachesZero() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val player = FakeExoPlayer.Builder(context).build()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            val nav = rememberNavController()
            PlayerScreen(navController = nav, testPlayer = player)
        }
        composeTestRule.onNodeWithText("Session (Minuten)").performTextInput("1")
        composeTestRule.runOnUiThread {
            player.setMediaSource(FakeMediaSource(FakeTimeline(1)))
            player.prepare()
        }
        composeTestRule.onNodeWithText("Play").performClick()
        composeTestRule.mainClock.advanceTimeBy(61_000)
        composeTestRule.onNodeWithText("Restzeit: 00:00").assertIsDisplayed()
    }
}
