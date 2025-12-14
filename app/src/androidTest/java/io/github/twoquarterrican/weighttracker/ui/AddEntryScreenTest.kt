package io.github.twoquarterrican.weighttracker.ui

import android.text.format.DateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
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
        val sessionState = mutableStateOf(1)
        var savedWeight = 0f

        composeTestRule.setContent {
            val session by remember { sessionState }
            
            if (session == 1) {
                AddEntryScreen(
                    initialWeight = null,
                    onSave = { weight, _ -> 
                        savedWeight = weight
                        sessionState.value = 2
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

        // Session 1
        composeTestRule.onNodeWithText("Weight (lbs)").performTextInput("150.0")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        // Session 2
        composeTestRule.onNodeWithText("150.0").assertExists()
        val expectedDateString = DateFormat.format("yyyy-MM-dd h:mm a", Date()).toString()
        composeTestRule.onNodeWithText(expectedDateString).assertExists()
    }

    @Test
    fun addEntryScreen_pastDate_defaultsToMinuteZero() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        composeTestRule.setContent {
            AddEntryScreen(
                initialWeight = null,
                onSave = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("Date & Time").performClick()
        device.wait(Until.findObject(By.text("OK")), 5000)

        // Find Current Year to click header
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val yearHeader = device.findObject(By.text(currentYear))
        
        if (yearHeader != null) {
            yearHeader.click()
            val pastYearObj = device.findObject(By.text("2022"))
            if (pastYearObj != null) pastYearObj.click()
        }
        
        val prevMonth = device.findObject(By.desc("Previous month"))
        if (prevMonth != null) prevMonth.click()

        device.findObject(By.text("OK")).click()
        device.wait(Until.findObject(By.text("OK")), 5000)

        val hasZeroMinute = device.findObject(By.textContains(":00")) != null || 
                            device.findObject(By.textContains(" 00")) != null ||
                            device.findObject(By.text("00")) != null
                            
        assertTrue("Time picker should show :00 minute for past date", hasZeroMinute)
        device.pressBack()
    }

    @Test
    fun addEntryScreen_currentDate_preservesTime() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        composeTestRule.setContent {
            AddEntryScreen(
                initialWeight = null,
                onSave = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("Date & Time").performClick()
        device.wait(Until.findObject(By.text("OK")), 5000)
        device.findObject(By.text("OK")).click()

        device.wait(Until.findObject(By.text("OK")), 5000)

        val calendar = Calendar.getInstance()
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        if (currentMinute != 0) {
            val minuteStr = "%02d".format(currentMinute)
            val hasCurrentMinute = device.findObject(By.textContains(":$minuteStr")) != null ||
                                   device.findObject(By.textContains(" $minuteStr")) != null
            assertTrue("Time picker should show current minute ($minuteStr) for today", hasCurrentMinute)
        }
        device.pressBack()
    }
}
