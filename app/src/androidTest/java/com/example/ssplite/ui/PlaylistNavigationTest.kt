package com.example.ssplite.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import com.example.ssplite.MainActivity
import org.junit.Rule
import org.junit.Test

class PlaylistNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigateToFilterAndBack() {
        composeTestRule.onNodeWithText("Filter").performClick()
        composeTestRule.onNodeWithText("Import").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zurück").performClick()
        composeTestRule.onNodeWithText("Filter").assertIsDisplayed()
    }
}
