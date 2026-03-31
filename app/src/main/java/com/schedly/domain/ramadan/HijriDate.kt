package com.schedly.domain.ramadan

data class HijriDate(val year: Int, val month: Int, val day: Int) {
    fun isRamadan(): Boolean = month == 9
}
