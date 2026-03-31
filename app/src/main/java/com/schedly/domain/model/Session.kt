package com.schedly.domain.model

import java.util.UUID

data class Session(
    val id: UUID = UUID.randomUUID(),
    val courseId: UUID,
    val type: SessionType,
    val group: String,
    val instructor: String,
    val day: DayOfWeek,
    val period: Int,
    val room: String,
    val priority: PriorityLevel
) {
    fun isValid(): Boolean {
        val allowedDays = setOf(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
        return period in 1..6 &&
                allowedDays.contains(day) &&
                group.isNotBlank() &&
                instructor.isNotBlank() &&
                room.isNotBlank()
    }

    fun roomOccupancyKey(): String = "${day.name}:$period:$room"
}
