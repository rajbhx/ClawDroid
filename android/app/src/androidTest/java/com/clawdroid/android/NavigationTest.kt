package com.clawdroid.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches() {
        composeRule.waitForIdle()
    }

    @Test
    fun chatScreenShowsTitle() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ClawDroid").assertIsDisplayed()
    }
}
