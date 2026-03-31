package com.schedly.domain.model

import java.util.UUID

data class Schedule(
    val id: UUID,
    val sessions: List<Session>,
    val isPartial: Boolean = false,
    val droppedSessions: List<SessionRef> = emptyList()
) {
    val weekLoad: Int = sessions.map { it.day }.distinct().size
    val dayLoad: Map<DayOfWeek, Int> = sessions.groupingBy { it.day }.eachCount()

    fun hasConflicts(): Boolean {
        val timeSlots = sessions.map { "${it.day.name}:${it.period}" }
        return timeSlots.size != timeSlots.distinct().size
    }

    fun sessionsByCourse(): Map<UUID, List<Session>> = sessions.groupBy { it.courseId }

    companion object {
        fun create(
            sessions: List<Session>,
            isPartial: Boolean = false,
            droppedSessions: List<SessionRef> = emptyList()
        ): Schedule {
            return Schedule(
                id = UUID.randomUUID(),
                sessions = sessions,
                isPartial = isPartial,
                droppedSessions = droppedSessions
            )
        }
    }
}
