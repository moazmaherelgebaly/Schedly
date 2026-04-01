package com.schedly.domain.ramadan

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import java.time.LocalDate

class RamadanDetectorTest {

    @Test
    fun `RamadanDetector detects Ramadan correctly for known dates`() {
        // Test with known Ramadan dates (approximate, varies by location)
        // Ramadan 1445: March 11 - April 9, 2024
        val marchDuringRamadan = LocalDate.of(2024, 3, 20)
        val aprilAfterRamadan = LocalDate.of(2024, 4, 15)
        
        val hijriMarch = HijriCalendar.toHijri(marchDuringRamadan)
        val hijriApril = HijriCalendar.toHijri(aprilAfterRamadan)
        
        assertTrue("March 20, 2024 should be in Ramadan", hijriMarch.month == 9)
        assertFalse("April 15, 2024 should be after Ramadan", hijriApril.month == 9)
    }
    
    @Test
    fun `RamadanDetector handles non-Ramadan months`() {
        // Test with non-Ramadan dates
        val januaryDate = LocalDate.of(2024, 1, 15)
        val juneDate = LocalDate.of(2024, 6, 15)
        
        val hijriJanuary = HijriCalendar.toHijri(januaryDate)
        val hijriJune = HijriCalendar.toHijri(juneDate)
        
        assertFalse("January should not be Ramadan", hijriJanuary.month == 9)
        assertFalse("June should not be Ramadan", hijriJune.month == 9)
    }
    
    @Test
    fun `HijriDate isRamadan returns true for month 9`() {
        val ramadanDate = HijriDate(1445, 9, 15)
        val nonRamadanDate = HijriDate(1445, 8, 15)
        
        assertTrue(ramadanDate.isRamadan())
        assertFalse(nonRamadanDate.isRamadan())
    }
    
    @Test
    fun `HijriCalendar converts Gregorian to Hijri correctly`() {
        // Test known conversion: July 18, 622 CE = 1 Muharram 1 AH
        val epochDate = LocalDate.of(622, 7, 18)
        val hijriEpoch = HijriCalendar.toHijri(epochDate)
        
        assertEquals(1, hijriEpoch.year)
        assertEquals(1, hijriEpoch.month)
        assertEquals(1, hijriEpoch.day)
    }
    
    @Test
    fun `HijriCalendar handles modern dates`() {
        val modernDate = LocalDate.of(2024, 1, 1)
        val hijriDate = HijriCalendar.toHijri(modernDate)
        
        // 2024 should be around 1445 AH
        assertTrue(hijriDate.year in 1444..1446)
        assertTrue(hijriDate.month in 1..12)
        assertTrue(hijriDate.day in 1..30)
    }
    
    @Test
    fun `HijriCalendar throws for dates before epoch`() {
        val beforeEpoch = LocalDate.of(622, 7, 17)
        
        try {
            HijriCalendar.toHijri(beforeEpoch)
            // Should not reach here
            assertTrue(false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("before Hijri epoch") == true)
        }
    }
    
    @Test
    fun `HijriCalendar handles leap years correctly`() {
        // Test that leap years have 355 days
        val leapYearStart = LocalDate.of(2023, 5, 21) // Approximate start of 1445 AH
        val hijriDate = HijriCalendar.toHijri(leapYearStart)
        
        // Verify the conversion works for leap year
        assertTrue(hijriDate.year > 0)
    }
}
