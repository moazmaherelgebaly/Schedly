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
        val (start1, end1) = HijriCalendar.getRamadanStartEnd(today.year)
        val adjustedStart1 = start1.plusDays(offset.toLong())
        val adjustedEnd1 = end1.plusDays(offset.toLong())

        if (today in adjustedStart1..adjustedEnd1) {
            return true
        }

        // Check previous year's Ramadan (may extend into current year)
        val (start2, end2) = HijriCalendar.getRamadanStartEnd(today.year - 1)
        val adjustedStart2 = start2.plusDays(offset.toLong())
        val adjustedEnd2 = end2.plusDays(offset.toLong())

        return today in adjustedStart2..adjustedEnd2
    }

    suspend fun setRamadanOffset(offset: Int) {
        preferencesRepository.setRamadanOffset(offset)
    }

    suspend fun getRamadanOffset(): Int {
        return preferencesRepository.getRamadanOffset()
    }
}
