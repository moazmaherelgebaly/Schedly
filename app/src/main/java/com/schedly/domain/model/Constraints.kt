package com.schedly.domain.model

import java.util.UUID

data class DayPeriod(val day: DayOfWeek, val period: Int)
data class SessionRef(val courseId: UUID, val type: SessionType, val group: String)
data class InstructorPref(val courseId: UUID, val type: SessionType, val instructor: String)
data class GroupPref(val courseId: UUID, val type: SessionType, val group: String)

data class Constraints(
    val weekLoadMin: Int? = null,
    val weekLoadMax: Int? = null,
    val dayLoadMin: Int? = null,
    val dayLoadMax: Int? = null,
    val excludedDayPeriods: List<DayPeriod> = emptyList(),
    val excludedSessions: List<SessionRef> = emptyList(),
    val allowGaps: Boolean = true,
    val matchGroups: Boolean = false,
    val preferredInstructors: List<InstructorPref> = emptyList(),
    val preferredGroups: List<GroupPref> = emptyList()
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (weekLoadMin != null && weekLoadMin !in 1..6) {
            errors.add("Week load must be between 1 and 6")
        }
        if (weekLoadMax != null && weekLoadMax !in 1..6) {
            errors.add("Week load must be between 1 and 6")
        }
        if (weekLoadMin != null && weekLoadMax != null && weekLoadMin > weekLoadMax) {
            errors.add("Week load min ($weekLoadMin) cannot exceed max ($weekLoadMax)")
        }

        if (dayLoadMin != null && dayLoadMin !in 0..6) {
            errors.add("Day load must be between 0 and 6")
        }
        if (dayLoadMax != null && dayLoadMax !in 0..6) {
            errors.add("Day load must be between 0 and 6")
        }
        if (dayLoadMin != null && dayLoadMax != null && dayLoadMin > dayLoadMax) {
            errors.add("Day load min ($dayLoadMin) cannot exceed max ($dayLoadMax)")
        }

        excludedDayPeriods.forEach { dp ->
            if (dp.period !in 1..6) {
                errors.add("Excluded period ${dp.period} must be between 1 and 6")
            }
        }

        return errors
    }
}
