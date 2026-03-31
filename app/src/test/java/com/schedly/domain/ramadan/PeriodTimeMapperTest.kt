package com.schedly.domain.ramadan

import com.schedly.domain.model.TimeMode
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class PeriodTimeMapperTest {

    @Test
    fun `PeriodTimeMapper returns correct Normal mode times for all periods`() {
        val normalTimes = mapOf(
            1 to Pair("08:30", "10:30"),
            2 to Pair("10:30", "12:30"),
            3 to Pair("12:30", "14:30"),
            4 to Pair("14:30", "16:30"),
            5 to Pair("16:30", "18:30"),
            6 to Pair("18:30", "20:30")
        )
        
        for ((period, expected) in normalTimes) {
            val actual = PeriodTimeMapper.getPeriodTime(period, TimeMode.Normal)
            assertEquals("Period $period Normal mode time mismatch", expected, actual)
        }
    }
    
    @Test
    fun `PeriodTimeMapper returns correct Ramadan mode times for all periods`() {
        val ramadanTimes = mapOf(
            1 to Pair("09:00", "10:15"),
            2 to Pair("10:15", "11:30"),
            3 to Pair("11:30", "12:45"),
            4 to Pair("12:45", "14:00"),
            5 to Pair("14:00", "15:15"),
            6 to Pair("15:15", "16:30")
        )
        
        for ((period, expected) in ramadanTimes) {
            val actual = PeriodTimeMapper.getPeriodTime(period, TimeMode.Ramadan)
            assertEquals("Period $period Ramadan mode time mismatch", expected, actual)
        }
    }
    
    @Test
    fun `PeriodTimeMapper throws for invalid period`() {
        try {
            PeriodTimeMapper.getPeriodTime(0, TimeMode.Normal)
            assertTrue(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Period must be 1-6") == true)
        }
        
        try {
            PeriodTimeMapper.getPeriodTime(7, TimeMode.Normal)
            assertTrue(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Period must be 1-6") == true)
        }
    }
    
    @Test
    fun `PeriodTimeMapper returns correct display string for Normal mode`() {
        val displayString = PeriodTimeMapper.getDisplayString(1, TimeMode.Normal)
        assertEquals("08:30 - 10:30", displayString)
    }
    
    @Test
    fun `PeriodTimeMapper returns correct display string for Ramadan mode`() {
        val displayString = PeriodTimeMapper.getDisplayString(1, TimeMode.Ramadan)
        assertEquals("09:00 - 10:15", displayString)
    }
    
    @Test
    fun `PeriodTimeMapper Ramadan periods are 1h15m each`() {
        for (period in 1..6) {
            val (start, end) = PeriodTimeMapper.getPeriodTime(period, TimeMode.Ramadan)
            val startMinutes = timeToMinutes(start)
            val endMinutes = timeToMinutes(end)
            val duration = endMinutes - startMinutes
            
            assertEquals("Ramadan period $period should be 75 minutes", 75, duration)
        }
    }
    
    @Test
    fun `PeriodTimeMapper Normal periods are 2h each`() {
        for (period in 1..6) {
            val (start, end) = PeriodTimeMapper.getPeriodTime(period, TimeMode.Normal)
            val startMinutes = timeToMinutes(start)
            val endMinutes = timeToMinutes(end)
            val duration = endMinutes - startMinutes
            
            assertEquals("Normal period $period should be 120 minutes", 120, duration)
        }
    }
    
    @Test
    fun `PeriodTimeMapper getAllPeriodTimes returns all 6 periods for Normal mode`() {
        val allTimes = PeriodTimeMapper.getAllPeriodTimes(TimeMode.Normal)
        
        assertEquals(6, allTimes.size)
        for (period in 1..6) {
            assertTrue("Period $period should be in map", allTimes.containsKey(period))
        }
    }
    
    @Test
    fun `PeriodTimeMapper getAllPeriodTimes returns all 6 periods for Ramadan mode`() {
        val allTimes = PeriodTimeMapper.getAllPeriodTimes(TimeMode.Ramadan)
        
        assertEquals(6, allTimes.size)
        for (period in 1..6) {
            assertTrue("Period $period should be in map", allTimes.containsKey(period))
        }
    }
    
    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}
