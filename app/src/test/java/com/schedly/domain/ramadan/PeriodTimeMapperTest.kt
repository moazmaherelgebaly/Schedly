package com.schedly.domain.ramadan

import com.schedly.domain.model.TimeMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for PeriodTimeMapper.
 * Tests all 12 combinations (6 periods × 2 modes) as per spec requirements.
 */
class PeriodTimeMapperTest {

    @Test
    fun `Normal mode - period 1 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(1, TimeMode.Normal)
        assertEquals("08:30" to "10:30", result)
    }

    @Test
    fun `Normal mode - period 2 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(2, TimeMode.Normal)
        assertEquals("10:30" to "12:30", result)
    }

    @Test
    fun `Normal mode - period 3 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(3, TimeMode.Normal)
        assertEquals("12:30" to "14:30", result)
    }

    @Test
    fun `Normal mode - period 4 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(4, TimeMode.Normal)
        assertEquals("14:30" to "16:30", result)
    }

    @Test
    fun `Normal mode - period 5 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(5, TimeMode.Normal)
        assertEquals("16:30" to "18:30", result)
    }

    @Test
    fun `Normal mode - period 6 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(6, TimeMode.Normal)
        assertEquals("18:30" to "20:30", result)
    }

    @Test
    fun `Ramadan mode - period 1 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(1, TimeMode.Ramadan)
        assertEquals("09:00" to "10:15", result)
    }

    @Test
    fun `Ramadan mode - period 2 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(2, TimeMode.Ramadan)
        assertEquals("10:15" to "11:30", result)
    }

    @Test
    fun `Ramadan mode - period 3 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(3, TimeMode.Ramadan)
        assertEquals("11:30" to "12:45", result)
    }

    @Test
    fun `Ramadan mode - period 4 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(4, TimeMode.Ramadan)
        assertEquals("12:45" to "14:00", result)
    }

    @Test
    fun `Ramadan mode - period 5 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(5, TimeMode.Ramadan)
        assertEquals("14:00" to "15:15", result)
    }

    @Test
    fun `Ramadan mode - period 6 returns correct times`() {
        val result = PeriodTimeMapper.getPeriodTime(6, TimeMode.Ramadan)
        assertEquals("15:15" to "16:30", result)
    }

    @Test
    fun `getPeriodTime throws exception for invalid period`() {
        try {
            PeriodTimeMapper.getPeriodTime(0, TimeMode.Normal)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Period must be 1-6, got 0", e.message)
        }

        try {
            PeriodTimeMapper.getPeriodTime(7, TimeMode.Normal)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Period must be 1-6, got 7", e.message)
        }
    }

    @Test
    fun `getDisplayString returns formatted time range for Normal mode`() {
        val result = PeriodTimeMapper.getDisplayString(1, TimeMode.Normal)
        assertEquals("08:30 - 10:30", result)
    }

    @Test
    fun `getDisplayString returns formatted time range for Ramadan mode`() {
        val result = PeriodTimeMapper.getDisplayString(1, TimeMode.Ramadan)
        assertEquals("09:00 - 10:15", result)
    }

    @Test
    fun `getAllPeriodTimes returns all 6 periods for Normal mode`() {
        val result = PeriodTimeMapper.getAllPeriodTimes(TimeMode.Normal)
        assertEquals(6, result.size)
        assertEquals("08:30" to "10:30", result[1])
        assertEquals("18:30" to "20:30", result[6])
    }

    @Test
    fun `getAllPeriodTimes returns all 6 periods for Ramadan mode`() {
        val result = PeriodTimeMapper.getAllPeriodTimes(TimeMode.Ramadan)
        assertEquals(6, result.size)
        assertEquals("09:00" to "10:15", result[1])
        assertEquals("15:15" to "16:30", result[6])
    }
}
