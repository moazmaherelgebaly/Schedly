package com.schedly.domain.engine

import com.schedly.domain.model.*

/**
 * Validates schedules against all 8 constraint types.
 * Hard constraints are checked during early pruning.
 * Soft constraints are checked on complete schedules.
 */
class ConstraintValidator {

    /**
     * Validate a complete schedule against all constraints.
     * Returns true if schedule passes all constraints.
     */
    fun validate(schedule: Schedule, constraints: Constraints): Boolean {
        if (!checkExcludedDayPeriods(schedule, constraints)) return false
        if (!checkExcludedSessions(schedule, constraints)) return false
        if (!checkMatchGroups(schedule, constraints)) return false
        if (!checkWeekLoad(schedule, constraints)) return false
        if (!checkDayLoad(schedule, constraints)) return false
        if (!checkAllowGaps(schedule, constraints)) return false
        if (!checkPreferredInstructors(schedule, constraints)) return false
        if (!checkPreferredGroups(schedule, constraints)) return false

        return true
    }
    
    /**
     * Check week load constraints (min/max distinct days).
     */
    private fun checkWeekLoad(schedule: Schedule, constraints: Constraints): Boolean {
        val distinctDays = schedule.weekLoad
        
        constraints.weekLoadMin?.let { min ->
            if (distinctDays < min) return false
        }
        
        constraints.weekLoadMax?.let { max ->
            if (distinctDays > max) return false
        }
        
        return true
    }
    
    /**
     * Check day load constraints (min/max sessions per day).
     */
    private fun checkDayLoad(schedule: Schedule, constraints: Constraints): Boolean {
        val sessionsPerDay = schedule.dayLoad
        
        constraints.dayLoadMin?.let { min ->
            // All days with sessions must have at least min sessions
            if (sessionsPerDay.any { it.value < min }) return false
        }
        
        constraints.dayLoadMax?.let { max ->
            // No day can have more than max sessions
            if (sessionsPerDay.any { it.value > max }) return false
        }
        
        return true
    }
    
    /**
     * Check excluded day-periods constraint.
     */
    private fun checkExcludedDayPeriods(schedule: Schedule, constraints: Constraints): Boolean {
        return schedule.sessions.all { session ->
            !constraints.excludedDayPeriods.any { excluded ->
                excluded.day == session.day && excluded.period == session.period
            }
        }
    }
    
    /**
     * Check excluded sessions constraint.
     */
    private fun checkExcludedSessions(schedule: Schedule, constraints: Constraints): Boolean {
        return schedule.sessions.all { session ->
            !constraints.excludedSessions.any { excluded ->
                excluded.courseId == session.courseId &&
                excluded.type == session.type &&
                excluded.group == session.group
            }
        }
    }
    
    /**
     * Check allow gaps constraint.
     * When allowGaps is false, reject any day with gaps between sessions.
     */
    private fun checkAllowGaps(schedule: Schedule, constraints: Constraints): Boolean {
        if (constraints.allowGaps) return true
        
        // Group sessions by day
        val sessionsByDay = schedule.sessions.groupBy { it.day }
        
        // Check each day for gaps
        for ((_, daySessions) in sessionsByDay) {
            val periods = daySessions.map { it.period }.sorted()
            
            // Check for gaps between consecutive periods
            for (i in 0 until periods.size - 1) {
                val currentPeriod = periods[i]
                val nextPeriod = periods[i + 1]
                
                // Gap exists if there's more than 1 period between
                if (nextPeriod - currentPeriod > 1) {
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * Check preferred instructor constraint.
     * Returns true if all sessions match preferred instructors (when specified).
     */
    fun checkPreferredInstructors(schedule: Schedule, constraints: Constraints): Boolean {
        // Group sessions by course and type
        val sessionsByCourseAndType = schedule.sessions.groupBy { 
            Pair(it.courseId, it.type) 
        }
        
        // Check each preferred instructor constraint
        for (pref in constraints.preferredInstructors) {
            val key = Pair(pref.courseId, pref.type)
            val sessions = sessionsByCourseAndType[key] ?: continue
            
            // All sessions for this course+type must be taught by preferred instructor
            if (sessions.any { it.instructor != pref.instructor }) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Check preferred group constraint.
     * Returns true if all sessions match preferred groups (when specified).
     */
    fun checkPreferredGroups(schedule: Schedule, constraints: Constraints): Boolean {
        // Group sessions by course and type
        val sessionsByCourseAndType = schedule.sessions.groupBy {
            Pair(it.courseId, it.type)
        }

        // Check each preferred group constraint
        for (pref in constraints.preferredGroups) {
            val key = Pair(pref.courseId, pref.type)
            val sessions = sessionsByCourseAndType[key] ?: continue

            // All sessions for this course+type must be in preferred group
            if (sessions.any { it.group != pref.group }) {
                return false
            }
        }

        return true
    }

    /**
     * Check match groups constraint.
     * Returns true if for each course, lecture and section groups match (when enabled).
     */
    fun checkMatchGroups(schedule: Schedule, constraints: Constraints): Boolean {
        if (!constraints.matchGroups) return true

        // Group sessions by course
        val sessionsByCourse = schedule.sessions.groupBy { it.courseId }

        // For each course, verify lecture and section groups match
        for ((courseId, courseSessions) in sessionsByCourse) {
            val lectures = courseSessions.filter { it.type == SessionType.LECTURE }
            val sections = courseSessions.filter { it.type == SessionType.SECTION }

            // Get all unique groups for lectures and sections
            val lectureGroups = lectures.map { it.group }.toSet()
            val sectionGroups = sections.map { it.group }.toSet()

            // For match groups to pass, each lecture-section pair must have matching groups
            // This means: for every lecture group, there must be a section with the same group
            for (lecture in lectures) {
                val matchingSection = sections.find { it.group == lecture.group }
                if (matchingSection == null) {
                    return false
                }
            }

            // Also check the reverse: every section must have a matching lecture
            for (section in sections) {
                val matchingLecture = lectures.find { it.group == section.group }
                if (matchingLecture == null) {
                    return false
                }
            }
        }

        return true
    }
}
