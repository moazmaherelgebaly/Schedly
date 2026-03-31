package com.schedly.domain.ramadan

import com.schedly.domain.model.TimeMode
import com.schedly.domain.repository.PreferencesRepository
import java.time.LocalDate

class RamadanDetector(
    private val preferencesRepository: PreferencesRepository
) {

    suspend fun getCurrentTimeMode(): TimeMode {
        return if (isRamadanToday()) TimeMode.Ramadan else TimeMode.Normal
    }

    suspend fun isRamadanToday(): Boolean {
        val today = LocalDate.now()
        val offset = preferencesRepository.getRamadanOffset()

        // Check Ramadan for current year and previous year (Ramadan can span two Gregorian years)
        val ramadanCurrentYear = HijriCalendar.getRamadanStartEnd(today.year)
        if (ramadanCurrentYear != null) {
            val (start1, end1) = ramadanCurrentYear
            val adjustedStart1 = start1.plusDays(offset.toLong())
            val adjustedEnd1 = end1.plusDays(offset.toLong())

            if (today in adjustedStart1..adjustedEnd1) {
                return true
            }
        }

        // Check previous year's Ramadan (may extend into current year)
        val ramadanPreviousYear = HijriCalendar.getRamadanStartEnd(today.year - 1)
        if (ramadanPreviousYear != null) {
            val (start2, end2) = ramadanPreviousYear
            val adjustedStart2 = start2.plusDays(offset.toLong())
            val adjustedEnd2 = end2.plusDays(offset.toLong())

            return today in adjustedStart2..adjustedEnd2
        }

        // If Ramadan dates cannot be determined, treat as "not Ramadan"
        return false
    }

    suspend fun setRamadanOffset(offset: Int) {
        preferencesRepository.setRamadanOffset(offset)
    }

    suspend fun getRamadanOffset(): Int {
        return preferencesRepository.getRamadanOffset()
    }
}
