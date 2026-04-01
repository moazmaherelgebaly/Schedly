package com.schedly.domain.engine

import com.schedly.domain.model.*

/**
 * Handles LOW-priority session drop logic.
 * Drops only the conflicting session type (lecture or section), not the entire course.
 */
class PriorityDropLogic {

    private val validator = ConstraintValidator()

    /**
     * Apply priority drop logic to a schedule with conflicts.
     * Returns a valid schedule with LOW-priority sessions dropped, or null if unrecoverable.
     */
    fun apply(schedule: Schedule, constraints: Constraints): Schedule? {
        if (!schedule.hasConflicts() && validator.validate(schedule, constraints)) {
            return schedule
        }

        val lowPrioritySessions = schedule.sessions.filter {
            it.priority == PriorityLevel.LOW
        }

        if (lowPrioritySessions.isEmpty()) {
            return null
        }

        val sessionsByCourse = schedule.sessions.groupBy { it.courseId }

        for ((courseId, courseSessions) in sessionsByCourse) {
            val lowSessionsInCourse = courseSessions.filter {
                it.priority == PriorityLevel.LOW
            }

            for (lowSession in lowSessionsInCourse) {
                val remainingSessions = courseSessions.filter { it != lowSession }

                val hasLecture = remainingSessions.any { it.type == SessionType.LECTURE }
                val hasSection = remainingSessions.any { it.type == SessionType.SECTION }

                if (!hasLecture || !hasSection) {
                    continue
                }

                val allOtherSessions = schedule.sessions.filter { it.courseId != courseId }
                val newSessions = allOtherSessions + remainingSessions

                if (hasTimeConflict(newSessions)) {
                    continue
                }

                val testSchedule = Schedule.create(newSessions)
                if (!validator.validate(testSchedule, constraints)) {
                    continue
                }

                val droppedRef = SessionRef(
                    courseId = lowSession.courseId,
                    type = lowSession.type,
                    group = lowSession.group
                )

                return Schedule.create(
                    sessions = newSessions,
                    isPartial = true,
                    droppedSessions = schedule.droppedSessions + droppedRef
                )
            }
        }

        return null
    }

    /**
     * Check if sessions have time conflicts.
     */
    private fun hasTimeConflict(sessions: List<Session>): Boolean {
        val timeSlots = sessions.map { "${it.day.name}:${it.period}" }
        return timeSlots.size != timeSlots.distinct().size
    }
}
