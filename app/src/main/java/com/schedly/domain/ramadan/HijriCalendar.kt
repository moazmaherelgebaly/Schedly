package com.schedly.domain.ramadan

import java.time.LocalDate

object HijriCalendar {

    private const val HIJRI_EPOCH_JULIAN_DAY = 1948439.5
    private const val DAYS_IN_30_YEAR_CYCLE = 10631

    private val STANDARD_MONTH_LENGTHS = intArrayOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29)
    private val LEAP_YEAR_POSITIONS = setOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)

    fun toHijri(gregorian: LocalDate): HijriDate {
        val epochDate = LocalDate.of(622, 7, 16)
        if (gregorian.isBefore(epochDate)) {
            return HijriDate(0, 0, 0)
        }
        if (gregorian.isEqual(epochDate)) {
            return HijriDate(1, 1, 1)
        }

        val julianDay = gregorian.toEpochDay() + 2440588.5
        val daysSinceEpoch = (julianDay - HIJRI_EPOCH_JULIAN_DAY).toInt()
        val cycleNumber = daysSinceEpoch / DAYS_IN_30_YEAR_CYCLE
        var remainingDays = daysSinceEpoch % DAYS_IN_30_YEAR_CYCLE

        var yearInCycle = 0
        for (i in 0 until 30) {
            val daysInYear = if (isLeapYear(i + 1)) 355 else 354
            if (remainingDays < daysInYear) {
                yearInCycle = i + 1
                break
            }
            remainingDays -= daysInYear
        }

        if (yearInCycle == 0) {
            yearInCycle = 30
        }

        val hijriYear = cycleNumber * 30 + yearInCycle

        var monthDaysRemaining = remainingDays
        var hijriMonth = 1
        var daysInCurrentMonth = getMonthLength(hijriMonth, hijriYear)

        while (monthDaysRemaining >= daysInCurrentMonth) {
            monthDaysRemaining -= daysInCurrentMonth
            hijriMonth++
            daysInCurrentMonth = getMonthLength(hijriMonth, hijriYear)
        }

        return HijriDate(hijriYear, hijriMonth, monthDaysRemaining + 1)
    }

    fun fromHijri(hijri: HijriDate): LocalDate {
        if (hijri.year <= 0) {
            return LocalDate.of(622, 7, 16)
        }

        var totalDays = 0
        val completeCycles = (hijri.year - 1) / 30
        totalDays += completeCycles * DAYS_IN_30_YEAR_CYCLE

        val yearsInCycle = (hijri.year - 1) % 30
        for (year in 1..yearsInCycle) {
            totalDays += if (isLeapYear(year)) 355 else 354
        }

        for (month in 1 until hijri.month) {
            totalDays += getMonthLength(month, hijri.year)
        }

        totalDays += hijri.day - 1

        val julianDay = HIJRI_EPOCH_JULIAN_DAY + totalDays
        return LocalDate.ofEpochDay((julianDay - 2440588.5).toLong())
    }

    fun getRamadanStartEnd(gregorianYear: Int): Pair<LocalDate, LocalDate> {
        // Search for Ramadan that falls within the given Gregorian year
        // Start from an approximate Hijri year and search nearby years
        val approxHijriYear = gregorianToHijriYear(gregorianYear)
        
        // Check Ramadan for hijriYear-1, hijriYear, and hijriYear+1 to find the one that falls in gregorianYear
        for (hijriYearOffset in -1..1) {
            val hijriYear = approxHijriYear + hijriYearOffset
            val ramadanStart = fromHijri(HijriDate(hijriYear, 9, 1))
            
            if (ramadanStart.year == gregorianYear) {
                return ramadanStart to ramadanStart.plusDays(29)
            }
        }
        
        // If no exact match found, throw an exception with diagnostic info
        val ramadanStart1 = fromHijri(HijriDate(approxHijriYear, 9, 1))
        val ramadanStart2 = fromHijri(HijriDate(approxHijriYear + 1, 9, 1))
        throw IllegalStateException(
            "Could not determine Ramadan dates for Gregorian year $gregorianYear. " +
            "approxHijriYear=$approxHijriYear, ramadanStart1=$ramadanStart1 (year=${ramadanStart1.year}), " +
            "ramadanStart2=$ramadanStart2 (year=${ramadanStart2.year})"
        )
    }

    fun isLeapYear(hijriYear: Int): Boolean {
        return hijriYear % 30 in LEAP_YEAR_POSITIONS
    }

    private fun getMonthLength(month: Int, year: Int): Int {
        return when {
            month in 1..12 -> {
                val baseLength = STANDARD_MONTH_LENGTHS[month - 1]
                if (month == 12 && isLeapYear(year)) 30 else baseLength
            }
            else -> 30
        }
    }

    private fun gregorianToHijriYear(gregorianYear: Int): Int {
        return ((gregorianYear - 622) * 33 / 32) + 1
    }

    fun getCurrentHijriYear(gregorianYear: Int): Int {
        return toHijri(LocalDate.of(gregorianYear, 1, 1)).year
    }
}
