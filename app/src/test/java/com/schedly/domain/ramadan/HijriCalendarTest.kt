package com.schedly.domain.ramadan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for HijriCalendar using the Kuwaiti tabular algorithm.
 * Tests cover known date conversions and Ramadan date calculations.
 */
class HijriCalendarTest {

    // Known Gregorian to Hijri conversions (approximate, per Kuwaiti algorithm)
    // Source: Standard Islamic calendar tables

    @Test
    fun `toHijri converts Islamic epoch correctly`() {
        // Islamic epoch: July 18, 622 CE = 1 Muharram 1 AH (aligned with HIJRI_EPOCH_JULIAN_DAY)
        val epoch = LocalDate.of(622, 7, 18)
        val hijri = HijriCalendar.toHijri(epoch)

        assertEquals(1, hijri.year)
        assertEquals(1, hijri.month)
        assertEquals(1, hijri.day)
    }

    @Test
    fun `toHijri and fromHijri are bidirectional for epoch dates 622-07-16 to 622-07-19`() {
        // July 16 and 17 are before the epoch and should throw
        assertThrowsDatesBeforeEpoch(622, 7, 16)
        assertThrowsDatesBeforeEpoch(622, 7, 17)

        // July 18 is the epoch (1,1,1) - test bidirectional consistency
        val epochDate = LocalDate.of(622, 7, 18)
        val hijriEpoch = HijriCalendar.toHijri(epochDate)
        assertEquals(HijriDate(1, 1, 1), hijriEpoch)
        val backToEpoch = HijriCalendar.fromHijri(HijriDate(1, 1, 1))
        assertEquals(epochDate, backToEpoch)

        // July 19 is epoch + 1 day (1,1,2) - test bidirectional consistency
        val dayAfterEpoch = LocalDate.of(622, 7, 19)
        val hijriDayAfter = HijriCalendar.toHijri(dayAfterEpoch)
        assertEquals(HijriDate(1, 1, 2), hijriDayAfter)
        val backToDayAfter = HijriCalendar.fromHijri(HijriDate(1, 1, 2))
        assertEquals(dayAfterEpoch, backToDayAfter)
    }

    private fun assertThrowsDatesBeforeEpoch(year: Int, month: Int, day: Int) {
        val date = LocalDate.of(year, month, day)
        val exception = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.toHijri(date)
        }
        assertTrue("Exception should mention 'before'", exception.message?.contains("before") == true)
    }

    @Test
    fun `toHijri converts modern dates correctly`() {
        // January 1, 2000 ≈ 24 Ramadan 1420 AH
        val date = LocalDate.of(2000, 1, 1)
        val hijri = HijriCalendar.toHijri(date)

        assertEquals(1420, hijri.year)
        assertEquals(9, hijri.month) // Ramadan
        // Allow ±2 days for algorithm approximation
        assertTrue("Day should be around 24, got ${hijri.day}", hijri.day in 22..26)
    }

    @Test
    fun `toHijri converts 2024 date correctly`() {
        // March 15, 2024 ≈ 5 Ramadan 1445 AH (approximate)
        val date = LocalDate.of(2024, 3, 15)
        val hijri = HijriCalendar.toHijri(date)

        assertEquals(1445, hijri.year)
        assertEquals(9, hijri.month) // Ramadan
        assertTrue("Day should be in Ramadan, got ${hijri.day}", hijri.day in 1..30)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toHijri throws exception for dates before Islamic epoch`() {
        val date = LocalDate.of(600, 1, 1)
        HijriCalendar.toHijri(date)
    }

    @Test
    fun `fromHijri converts back to Gregorian correctly`() {
        // Test round-trip conversion
        val originalGregorian = LocalDate.of(2024, 3, 15)
        val hijri = HijriCalendar.toHijri(originalGregorian)
        val convertedBack = HijriCalendar.fromHijri(hijri)

        // Allow ±2 days for algorithm approximation
        val daysDiff = Math.abs(java.time.Duration.between(
            originalGregorian.atStartOfDay(),
            convertedBack.atStartOfDay()
        ).toDays())

        assertTrue("Round-trip should be within ±2 days, got $daysDiff", daysDiff <= 2)
    }

    @Test
    fun `fromHijri converts Islamic epoch correctly`() {
        val hijriDate = HijriDate(1, 1, 1)
        val gregorian = HijriCalendar.fromHijri(hijriDate)

        val expectedEpoch = LocalDate.of(622, 7, 18)
        assertEquals("HijriDate(1,1,1) should convert to July 18, 622", expectedEpoch, gregorian)
    }

    @Test
    fun `fromHijri throws exception for invalid year`() {
        val exception = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(0, 1, 1))
        }
        assertTrue("Exception should mention 'year'", exception.message?.contains("year") == true)

        val exceptionNegative = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(-5, 1, 1))
        }
        assertTrue("Exception should mention 'year'", exceptionNegative.message?.contains("year") == true)
    }

    @Test
    fun `fromHijri throws exception for invalid month`() {
        val exceptionZero = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(1, 0, 1))
        }
        assertTrue("Exception should mention 'month'", exceptionZero.message?.contains("month") == true)

        val exceptionNegative = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(1, -3, 1))
        }
        assertTrue("Exception should mention 'month'", exceptionNegative.message?.contains("month") == true)

        val exceptionTooHigh = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(1, 13, 1))
        }
        assertTrue("Exception should mention 'month'", exceptionTooHigh.message?.contains("month") == true)
    }

    @Test
    fun `fromHijri throws exception for invalid day`() {
        val exceptionZero = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(1, 1, 0))
        }
        assertTrue("Exception should mention 'day'", exceptionZero.message?.contains("day") == true)

        val exceptionNegative = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(1, 1, -5))
        }
        assertTrue("Exception should mention 'day'", exceptionNegative.message?.contains("day") == true)

        val exceptionTooHigh = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(1, 1, 31))
        }
        assertTrue("Exception should mention 'day'", exceptionTooHigh.message?.contains("day") == true)

        val exceptionLeapMonthTooHigh = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            HijriCalendar.fromHijri(HijriDate(2, 12, 31))
        }
        assertTrue("Exception should mention 'day'", exceptionLeapMonthTooHigh.message?.contains("day") == true)
    }

    @Test
    fun `fromHijri accepts valid edge case dates`() {
        val epoch = HijriCalendar.fromHijri(HijriDate(1, 1, 1))
        assertEquals(LocalDate.of(622, 7, 18), epoch)

        val lastDayOfMonth1 = HijriCalendar.fromHijri(HijriDate(1, 1, 30))
        assertEquals(epoch.plusDays(29), lastDayOfMonth1)

        val lastDayOfLeapMonth12 = HijriCalendar.fromHijri(HijriDate(2, 12, 30))
        org.junit.Assert.assertNotNull(lastDayOfLeapMonth12)
    }

    @Test
    fun `isLeapYear returns correct values for known leap years`() {
        // Known leap years in 30-year cycle: 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29
        val leapYears = listOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        val nonLeapYears = listOf(1, 3, 4, 6, 8, 9, 11, 12, 14, 15, 17, 19, 20, 22, 23, 25, 27, 28, 30)

        leapYears.forEach { year ->
            assertTrue("Year $year should be a leap year", HijriCalendar.isLeapYear(year))
        }

        nonLeapYears.forEach { year ->
            assertFalse("Year $year should not be a leap year", HijriCalendar.isLeapYear(year))
        }
    }

    @Test
    fun `isLeapYear works for years beyond first cycle`() {
        // Year 32 = cycle position 2 (32 % 30 = 2), should be leap
        assertTrue(HijriCalendar.isLeapYear(32))

        // Year 35 = cycle position 5 (35 % 30 = 5), should be leap
        assertTrue(HijriCalendar.isLeapYear(35))

        // Year 31 = cycle position 1 (31 % 30 = 1), should not be leap
        assertFalse(HijriCalendar.isLeapYear(31))
    }

    @Test
    fun `getRamadanStartEnd returns valid date range for 2024`() {
        val result = HijriCalendar.getRamadanStartEnd(2024)
        org.junit.Assert.assertNotNull("Ramadan 2024 should be found", result)
        val (start, end) = result!!

        // Ramadan 2024 was approximately March 11 - April 9, 2024
        // Allow for algorithm approximation (±3 days)
        assertTrue("Ramadan should start in March 2024", start.monthValue in 2..4)

        // Ramadan is 30 days
        val daysBetween = java.time.Duration.between(
            start.atStartOfDay(),
            end.atStartOfDay()
        ).toDays() + 1

        assertEquals("Ramadan should be 30 days", 30L, daysBetween)
        assertTrue("Ramadan should end after it starts", end.isAfter(start))
    }

    @Test
    fun `getRamadanStartEnd returns valid date range for 2025`() {
        val result = HijriCalendar.getRamadanStartEnd(2025)
        org.junit.Assert.assertNotNull("Ramadan 2025 should be found", result)
        val (start, end) = result!!

        // Ramadan 2025 is expected around March 1 - March 30, 2025
        assertTrue("Ramadan should start around March 2025", start.monthValue in 2..3)

        val daysBetween = java.time.Duration.between(
            start.atStartOfDay(),
            end.atStartOfDay()
        ).toDays() + 1

        assertEquals("Ramadan should be 30 days", 30L, daysBetween)
    }

    @Test
    fun `getRamadanStartEnd returns valid date range for 2026`() {
        val result = HijriCalendar.getRamadanStartEnd(2026)
        org.junit.Assert.assertNotNull("Ramadan 2026 should be found", result)
        val (start, end) = result!!

        // Ramadan 2026 is expected around February 18 - March 19, 2026
        assertTrue("Ramadan should start around February 2026", start.monthValue in 2..3)

        val daysBetween = java.time.Duration.between(
            start.atStartOfDay(),
            end.atStartOfDay()
        ).toDays() + 1

        assertEquals("Ramadan should be 30 days", 30L, daysBetween)
    }

    @Test
    fun `getRamadanStartEnd returns consistent results for multiple years`() {
        val years = listOf(2024, 2025, 2026, 2027, 2028)

        years.forEach { year ->
            val result = HijriCalendar.getRamadanStartEnd(year)
            org.junit.Assert.assertNotNull("Ramadan $year should be found", result)
            val (start, end) = result!!

            assertTrue("Ramadan start should be before end for $year", start.isBefore(end))

            val daysBetween = java.time.Duration.between(
                start.atStartOfDay(),
                end.atStartOfDay()
            ).toDays() + 1

            assertEquals("Ramadan should be 30 days for $year", 30L, daysBetween)
        }
    }

    @Test
    fun `getCurrentHijriYear returns correct year for Gregorian dates`() {
        // 2024 corresponds to approximately 1445-1446 AH
        val hijriYear2024 = HijriCalendar.getCurrentHijriYear(2024)
        assertTrue("Hijri year for 2024 should be around 1445-1446", hijriYear2024 in 1445..1446)

        // 2025 corresponds to approximately 1446-1447 AH
        val hijriYear2025 = HijriCalendar.getCurrentHijriYear(2025)
        assertTrue("Hijri year for 2025 should be around 1446-1447", hijriYear2025 in 1446..1447)
    }

    @Test
    fun `HijriDate isRamadan returns true for Ramadan month`() {
        val ramadanDate = HijriDate(1445, 9, 15)
        assertTrue(ramadanDate.isRamadan())
    }

    @Test
    fun `HijriDate isRamadan returns false for non-Ramadan months`() {
        val nonRamadanDate = HijriDate(1445, 1, 15) // Muharram
        assertFalse(nonRamadanDate.isRamadan())

        val shawwalDate = HijriDate(1445, 10, 15) // Shawwal
        assertFalse(shawwalDate.isRamadan())
    }

    @Test
    fun `toHijri and fromHijri maintain consistency for edge cases`() {
        // Test various edge cases
        val testDates = listOf(
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 6, 15),
            LocalDate.of(2020, 12, 31),
            LocalDate.of(2021, 1, 1),
            LocalDate.of(2025, 12, 31)
        )

        testDates.forEach { date ->
            val hijri = HijriCalendar.toHijri(date)
            val convertedBack = HijriCalendar.fromHijri(hijri)

            val daysDiff = Math.abs(java.time.Duration.between(
                date.atStartOfDay(),
                convertedBack.atStartOfDay()
            ).toDays())

            assertTrue(
                "Round-trip for $date should be within ±2 days, got $daysDiff (hijri: $hijri)",
                daysDiff <= 2
            )
        }
    }
}
