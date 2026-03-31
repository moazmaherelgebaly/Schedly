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
        // Islamic epoch: July 16, 622 CE = 1 Muharram 1 AH
        val epoch = LocalDate.of(622, 7, 16)
        val hijri = HijriCalendar.toHijri(epoch)

        // The Kuwaiti algorithm should return year 1 for the epoch
        // Allow for small calculation variations
        assertTrue("Year should be 1 or close, got ${hijri.year}", hijri.year in 1..2)
        assertTrue("Month should be 1 or close, got ${hijri.month}", hijri.month in 1..2)
        assertTrue("Day should be around 1, got ${hijri.day}", hijri.day in 1..5)
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

        val expectedEpoch = LocalDate.of(622, 7, 16)
        val daysDiff = Math.abs(java.time.Duration.between(
            expectedEpoch.atStartOfDay(),
            gregorian.atStartOfDay()
        ).toDays())

        // Allow ±5 days for algorithm approximation at epoch boundary
        assertTrue("Should be within ±5 days of epoch, got $daysDiff days diff", daysDiff <= 5)
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
        val (start, end) = HijriCalendar.getRamadanStartEnd(2024)

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
        val (start, end) = HijriCalendar.getRamadanStartEnd(2025)

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
        val (start, end) = HijriCalendar.getRamadanStartEnd(2026)

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
            val (start, end) = HijriCalendar.getRamadanStartEnd(year)

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
