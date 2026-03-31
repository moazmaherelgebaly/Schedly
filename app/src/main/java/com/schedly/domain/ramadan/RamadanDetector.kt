package com.schedly.domain.ramadan

import com.schedly.data.datastore.PreferencesManager
import com.schedly.domain.model.TimeMode
import java.time.LocalDate

class RamadanDetector(
    private val preferencesManager: PreferencesManager
) {

    suspend fun getCurrentTimeMode(): TimeMode {
        return if (isRamadanToday()) TimeMode.Ramadan else TimeMode.Normal
    }

    suspend fun isRamadanToday(): Boolean {
        val today = LocalDate.now()
        val offset = preferencesManager.getRamadanOffset()
        val (start, end) = HijriCalendar.getRamadanStartEnd(today.year)

        val adjustedStart = start.plusDays(offset.toLong())
        val adjustedEnd = end.plusDays(offset.toLong())

        return today in adjustedStart..adjustedEnd
    }

    suspend fun setRamadanOffset(offset: Int) {
        preferencesManager.setRamadanOffset(offset)
    }

    suspend fun getRamadanOffset(): Int {
        return preferencesManager.getRamadanOffset()
    }
}
