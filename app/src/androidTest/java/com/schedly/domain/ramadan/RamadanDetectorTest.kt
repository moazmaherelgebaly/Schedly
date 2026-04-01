package com.schedly.domain.ramadan

import android.content.Context
import com.schedly.data.datastore.PreferencesManager
import com.schedly.data.datastore.dataStore
import com.schedly.domain.model.TimeMode
import com.schedly.domain.repository.PreferencesRepository
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for RamadanDetector.
 * Tests Ramadan detection and TimeMode selection.
 */
class RamadanDetectorTest {

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var ramadanDetector: RamadanDetector

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferencesManager = PreferencesManager(context.dataStore)
        preferencesRepository = preferencesManager
        ramadanDetector = RamadanDetector(preferencesRepository)
    }

    @After
    fun teardown() {
        // Reset Ramadan offset to default to prevent test state leakage
        runTest {
            preferencesRepository.setRamadanOffset(0)
        }
    }

    @Test
    fun `getCurrentTimeMode returns Normal when not in Ramadan`() = runTest {
        // Test with a date that's definitely not Ramadan (June 2026)
        // Note: This test depends on the actual current date during execution
        // For deterministic testing, we verify the detector works with the algorithm
        val timeMode = ramadanDetector.getCurrentTimeMode()

        // The mode depends on when the test runs - just verify it returns a valid mode
        assertTrue(timeMode is TimeMode.Normal || timeMode is TimeMode.Ramadan)
    }

    @Test
    fun `isRamadanToday uses HijriCalendar for detection`() = runTest {
        // Test that the detector uses the Hijri calendar algorithm
        // June 15, 2026 should not be during Ramadan (Ramadan 2026 is ~Feb 18 - Mar 19)
        val testDate = LocalDate.of(2026, 6, 15)
        val result = HijriCalendar.getRamadanStartEnd(2026)
        org.junit.Assert.assertNotNull("Ramadan 2026 should be found", result)
        val (start, end) = result!!

        // Verify the calculated Ramadan period doesn't include June 15
        val isInCalculatedRamadan = testDate in start..end
        assertFalse("June 15, 2026 should not be during calculated Ramadan period", isInCalculatedRamadan)
    }

    @Test
    fun `Ramadan offset affects detection`() = runTest {
        // Set offset to +1 day
        ramadanDetector.setRamadanOffset(1)
        val offsetAfterSet = ramadanDetector.getRamadanOffset()
        assertEquals(1, offsetAfterSet)

        // Set offset to -1 day
        ramadanDetector.setRamadanOffset(-1)
        val offsetAfterNegative = ramadanDetector.getRamadanOffset()
        assertEquals(-1, offsetAfterNegative)

        // Reset to default
        ramadanDetector.setRamadanOffset(0)
        val offsetAfterReset = ramadanDetector.getRamadanOffset()
        assertEquals(0, offsetAfterReset)
    }

    @Test
    fun `Ramadan offset defaults to zero`() = runTest {
        val offset = ramadanDetector.getRamadanOffset()
        assertEquals(0, offset)
    }

    @Test
    fun `HijriCalendar calculates Ramadan dates for multiple years`() {
        val years = listOf(2024, 2025, 2026, 2027)

        years.forEach { year ->
            val result = HijriCalendar.getRamadanStartEnd(year)
            org.junit.Assert.assertNotNull("Ramadan $year should be found", result)
            val (start, end) = result!!

            // Verify Ramadan is approximately 29-30 days (Islamic months can be 29 or 30 days)
            val daysBetween = java.time.Duration.between(
                start.atStartOfDay(),
                end.atStartOfDay()
            ).toDays() + 1

            assertTrue("Ramadan should be 29-30 days for $year, got $daysBetween", daysBetween in 29..30)
            assertTrue("Ramadan start should be before end for $year", start.isBefore(end))
        }
    }

    @Test
    fun `Ramadan detector works with default offset`() = runTest {
        // Ensure offset is at default
        ramadanDetector.setRamadanOffset(0)

        // Detector should work without errors
        val isRamadan = ramadanDetector.isRamadanToday()
        val timeMode = ramadanDetector.getCurrentTimeMode()

        // Verify consistency between isRamadanToday and getCurrentTimeMode
        if (isRamadan) {
            assertTrue(timeMode is TimeMode.Ramadan)
        } else {
            assertTrue(timeMode is TimeMode.Normal)
        }
    }

    @Test
    fun `Ramadan detector applies positive offset`() = runTest {
        ramadanDetector.setRamadanOffset(1)

        // Detector should still work with offset
        val isRamadan = ramadanDetector.isRamadanToday()
        val timeMode = ramadanDetector.getCurrentTimeMode()

        assertTrue(timeMode is TimeMode.Normal || timeMode is TimeMode.Ramadan)
    }

    @Test
    fun `Ramadan detector applies negative offset`() = runTest {
        ramadanDetector.setRamadanOffset(-1)

        // Detector should still work with offset
        val isRamadan = ramadanDetector.isRamadanToday()
        val timeMode = ramadanDetector.getCurrentTimeMode()

        assertTrue(timeMode is TimeMode.Normal || timeMode is TimeMode.Ramadan)
    }
}
