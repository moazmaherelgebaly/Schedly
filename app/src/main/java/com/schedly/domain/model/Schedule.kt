package com.schedly.domain.model

import java.util.UUID

data class Schedule(
    val id: UUID = UUID.randomUUID(),
    val sessions: List<Session>,
    val weekLoad: Int = sessions.map { it.day }.distinct().size,
    val dayLoad: Map<DayOfWeek, Int> = sessions.groupingBy { it.day }.eachCount(),
    val isPartial: Boolean = false,
    val droppedSessions: List<SessionRef> = emptyList()
) {
    fun hasConflicts(): Boolean {
        val timeSlots = sessions.map { "${it.day.name}:${it.period}" }
        return timeSlots.size != timeSlots.distinct().size
    }

    fun sessionsByCourse(): Map<UUID, List<Session>> = sessions.groupBy { it.courseId }
}
