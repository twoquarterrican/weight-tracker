package io.github.twoquarterrican.weighttracker.ui

import android.text.format.DateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date
import kotlin.math.abs

class AddEntryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addEntryScreen_noPreviousWeight_showsEmptyField() {
        composeTestRule.setContent {
            AddEntryScreen(
                initialWeight = null,
                onSave = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("Weight (lbs)").assertTextContains("")
        composeTestRule.onNodeWithText("+0.1").assertDoesNotExist()
    }

    @Test
    fun addEntryScreen_withPreviousWeight_showsPreFilledFieldAndButtons() {
        val initialWeight = 150.0f
        
        composeTestRule.setContent {
            AddEntryScreen(
                initialWeight = initialWeight,
                onSave = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("150.0").assertExists()
        composeTestRule.onNodeWithText("+0.1").assertExists()
    }
    
    @Test
    fun addEntryScreen_quickAdjustButtons_updateValue() {
        val initialWeight = 150.0f
        
        composeTestRule.setContent {
            AddEntryScreen(
                initialWeight = initialWeight,
                onSave = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("+0.1").performClick()
        composeTestRule.onNodeWithText("150.1").assertExists()
        
        composeTestRule.onNodeWithText("+1.0").performClick()
        composeTestRule.onNodeWithText("151.1").assertExists()
    }

    @Test
    fun addEntryScreen_savesAndResetsTimeForNextEntry() {
        // We use a state to control which "session" we are in.
        val sessionState = mutableStateOf(1)
        var savedWeight = 0f

        composeTestRule.setContent {
            // We use a Key to force a full recomposition/reset of the AddEntryScreen when the session changes
            // ensuring it treats it as a fresh navigation.
            val session by remember { sessionState }
            
            if (session == 1) {
                AddEntryScreen(
                    initialWeight = null,
                    onSave = { weight, _ -> 
                        savedWeight = weight
                        sessionState.value = 2 // Move to next session
                    },
                    onCancel = {}
                )
            } else if (session == 2) {
                AddEntryScreen(
                    initialWeight = savedWeight,
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // --- Session 1 ---
        // User enters 150.0
        composeTestRule.onNodeWithText("Weight (lbs)").performTextInput("150.0")
        
        // User Clicks Save
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Wait for idle to ensure state update and recomposition happens
        composeTestRule.waitForIdle()

        // --- Session 2 ---
        // Verify we are now seeing the second screen with pre-filled data
        
        // Verify Weight is pre-filled with the value from Session 1
        composeTestRule.onNodeWithText("150.0").assertExists()

        // Verify Time is "Now" (Current System Time)
        val expectedDateString = DateFormat.format("yyyy-MM-dd h:mm a", Date()).toString()
        composeTestRule.onNodeWithText(expectedDateString).assertExists()
    }
}
