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

        // Subtract offset from today to get adjusted date, then check if it falls in Ramadan
        val adjustedDate = today.minusDays(offset.toLong())
        val hijriDate = HijriCalendar.toHijri(adjustedDate)

        return hijriDate.month == 9
    }

    suspend fun setRamadanOffset(offset: Int) {
        preferencesRepository.setRamadanOffset(offset)
    }

    suspend fun getRamadanOffset(): Int {
        return preferencesRepository.getRamadanOffset()
    }
}
