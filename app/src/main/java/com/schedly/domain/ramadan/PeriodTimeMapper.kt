package com.schedly.domain.ramadan

import com.schedly.domain.model.TimeMode

object PeriodTimeMapper {

    private val normalTimes = mapOf(
        1 to Pair("08:30", "10:30"),
        2 to Pair("10:30", "12:30"),
        3 to Pair("12:30", "14:30"),
        4 to Pair("14:30", "16:30"),
        5 to Pair("16:30", "18:30"),
        6 to Pair("18:30", "20:30")
    )

    private val ramadanTimes = mapOf(
        1 to Pair("09:00", "10:15"),
        2 to Pair("10:15", "11:30"),
        3 to Pair("11:30", "12:45"),
        4 to Pair("12:45", "14:00"),
        5 to Pair("14:00", "15:15"),
        6 to Pair("15:15", "16:30")
    )

    fun getPeriodTime(period: Int, mode: TimeMode): Pair<String, String> {
        require(period in 1..6) { "Period must be 1-6, got $period" }

        return when (mode) {
            is TimeMode.Normal -> normalTimes[period]!!
            is TimeMode.Ramadan -> ramadanTimes[period]!!
        }
    }

    suspend fun getPeriodTimeForToday(period: Int, ramadanDetector: RamadanDetector): Pair<String, String> {
        return getPeriodTime(period, ramadanDetector.getCurrentTimeMode())
    }

    fun getDisplayString(period: Int, mode: TimeMode): String {
        val (start, end) = getPeriodTime(period, mode)
        return "$start - $end"
    }

    suspend fun getDisplayStringForToday(period: Int, ramadanDetector: RamadanDetector): String {
        return getDisplayString(period, ramadanDetector.getCurrentTimeMode())
    }

    fun getAllPeriodTimes(mode: TimeMode): Map<Int, Pair<String, String>> {
        return when (mode) {
            is TimeMode.Normal -> normalTimes
            is TimeMode.Ramadan -> ramadanTimes
        }
    }
}
