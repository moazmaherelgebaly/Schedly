package com.schedly.domain.engine

import com.schedly.domain.model.*

/**
 * Diagnoses zero-result scenarios.
 * Identifies which constraint eliminated the most candidates.
 */
class DiagnosticsEngine {

    /**
     * Diagnose why zero schedules were found.
     * Returns diagnostic info with the most restrictive constraint.
     */
    fun diagnose(
        courses: List<Course>,
        constraints: Constraints,
        baselineCount: Int,
        generator: ScheduleGenerator
    ): DiagnosticInfo {
        if (baselineCount == 0) {
            return DiagnosticInfo.create(
                mostRestrictiveConstraint = "Input Data",
                baselineCount = 0,
                constraintImpacts = emptyMap()
            )
        }

        val constraintImpacts = mutableMapOf<String, Int>()

        if (constraints.weekLoadMin != null || constraints.weekLoadMax != null) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "Week Load",
                testConstraints = constraints.copy(
                    weekLoadMin = constraints.weekLoadMin,
                    weekLoadMax = constraints.weekLoadMax,
                    dayLoadMin = null,
                    dayLoadMax = null,
                    excludedDayPeriods = emptyList(),
                    excludedSessions = emptyList(),
                    allowGaps = true,
                    matchGroups = false,
                    preferredInstructors = emptyList(),
                    preferredGroups = emptyList()
                ),
                generator = generator
            )
            constraintImpacts["Week Load"] = baselineCount - count
        }

        if (constraints.dayLoadMin != null || constraints.dayLoadMax != null) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "Day Load",
                testConstraints = constraints.copy(
                    weekLoadMin = null,
                    weekLoadMax = null,
                    dayLoadMin = constraints.dayLoadMin,
                    dayLoadMax = constraints.dayLoadMax,
                    excludedDayPeriods = emptyList(),
                    excludedSessions = emptyList(),
                    allowGaps = true,
                    matchGroups = false,
                    preferredInstructors = emptyList(),
                    preferredGroups = emptyList()
                ),
                generator = generator
            )
            constraintImpacts["Day Load"] = baselineCount - count
        }

        if (constraints.excludedDayPeriods.isNotEmpty()) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "Excluded Day-Periods",
                testConstraints = constraints.copy(
                    weekLoadMin = null,
                    weekLoadMax = null,
                    dayLoadMin = null,
                    dayLoadMax = null,
                    excludedDayPeriods = constraints.excludedDayPeriods,
                    excludedSessions = emptyList(),
                    allowGaps = true,
                    matchGroups = false,
                    preferredInstructors = emptyList(),
                    preferredGroups = emptyList()
                ),
                generator = generator
            )
            constraintImpacts["Excluded Day-Periods"] = baselineCount - count
        }

        if (constraints.excludedSessions.isNotEmpty()) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "Excluded Sessions",
                testConstraints = constraints.copy(
                    weekLoadMin = null,
                    weekLoadMax = null,
                    dayLoadMin = null,
                    dayLoadMax = null,
                    excludedDayPeriods = emptyList(),
                    excludedSessions = constraints.excludedSessions,
                    allowGaps = true,
                    matchGroups = false,
                    preferredInstructors = emptyList(),
                    preferredGroups = emptyList()
                ),
                generator = generator
            )
            constraintImpacts["Excluded Sessions"] = baselineCount - count
        }

        if (!constraints.allowGaps) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "No Gaps Allowed",
                testConstraints = constraints.copy(
                    weekLoadMin = null,
                    weekLoadMax = null,
                    dayLoadMin = null,
                    dayLoadMax = null,
                    excludedDayPeriods = emptyList(),
                    excludedSessions = emptyList(),
                    allowGaps = false,
                    matchGroups = false,
                    preferredInstructors = emptyList(),
                    preferredGroups = emptyList()
                ),
                generator = generator
            )
            constraintImpacts["No Gaps Allowed"] = baselineCount - count
        }

        if (constraints.matchGroups) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "Match Groups",
                testConstraints = constraints.copy(
                    weekLoadMin = null,
                    weekLoadMax = null,
                    dayLoadMin = null,
                    dayLoadMax = null,
                    excludedDayPeriods = emptyList(),
                    excludedSessions = emptyList(),
                    allowGaps = true,
                    matchGroups = true,
                    preferredInstructors = emptyList(),
                    preferredGroups = emptyList()
                ),
                generator = generator
            )
            constraintImpacts["Match Groups"] = baselineCount - count
        }

        if (constraints.preferredInstructors.isNotEmpty()) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "Preferred Instructors",
                testConstraints = constraints.copy(
                    weekLoadMin = null,
                    weekLoadMax = null,
                    dayLoadMin = null,
                    dayLoadMax = null,
                    excludedDayPeriods = emptyList(),
                    excludedSessions = emptyList(),
                    allowGaps = true,
                    matchGroups = false,
                    preferredInstructors = constraints.preferredInstructors,
                    preferredGroups = emptyList()
                ),
                generator = generator
            )
            constraintImpacts["Preferred Instructors"] = baselineCount - count
        }

        if (constraints.preferredGroups.isNotEmpty()) {
            val count = testWithSingleConstraint(
                courses = courses,
                baseConstraints = constraints,
                constraintName = "Preferred Groups",
                testConstraints = constraints.copy(
                    weekLoadMin = null,
                    weekLoadMax = null,
                    dayLoadMin = null,
                    dayLoadMax = null,
                    excludedDayPeriods = emptyList(),
                    excludedSessions = emptyList(),
                    allowGaps = true,
                    matchGroups = false,
                    preferredInstructors = emptyList(),
                    preferredGroups = constraints.preferredGroups
                ),
                generator = generator
            )
            constraintImpacts["Preferred Groups"] = baselineCount - count
        }

        val mostRestrictive = constraintImpacts.maxByOrNull { it.value }

        return if (mostRestrictive != null && mostRestrictive.value > 0) {
            DiagnosticInfo.create(
                mostRestrictiveConstraint = mostRestrictive.key,
                baselineCount = baselineCount,
                constraintImpacts = constraintImpacts
            )
        } else {
            DiagnosticInfo.create(
                mostRestrictiveConstraint = "Unknown",
                baselineCount = baselineCount,
                constraintImpacts = constraintImpacts
            )
        }
    }

    /**
     * Test generation with a single constraint enabled.
     * Counts valid combinations by enumerating candidate pairs and validating them.
     */
    private fun testWithSingleConstraint(
        courses: List<Course>,
        baseConstraints: Constraints,
        constraintName: String,
        testConstraints: Constraints,
        generator: ScheduleGenerator
    ): Int {
        return try {
            countValidCombinations(courses, testConstraints, generator)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Count valid schedule combinations by enumerating candidate pairs.
     * This avoids triggering the full diagnostic cycle.
     */
    private fun countValidCombinations(
        courses: List<Course>,
        constraints: Constraints,
        generator: ScheduleGenerator
    ): Int {
        // Build candidate pairs for each course (applying filters like preferred instructor/group)
        val candidatePairsByCourse = courses.associate { course ->
            val lectureCandidates = filterSessionCandidates(
                sessions = course.lectures,
                courseId = course.id,
                type = SessionType.LECTURE,
                constraints = constraints
            )

            val sectionCandidates = filterSessionCandidates(
                sessions = course.sections,
                courseId = course.id,
                type = SessionType.SECTION,
                constraints = constraints
            )

            val pairs = mutableListOf<Pair<Session, Session>>()
            for (lecture in lectureCandidates) {
                for (section in sectionCandidates) {
                    if (constraints.matchGroups) {
                        if (lecture.group == section.group) {
                            pairs.add(Pair(lecture, section))
                        }
                    } else {
                        pairs.add(Pair(lecture, section))
                    }
                }
            }
            course to pairs
        }

        // Check if any course has no candidates
        if (candidatePairsByCourse.any { it.value.isEmpty() }) {
            return 0
        }

        // Count valid combinations using recursive enumeration
        return countValidCombinationsRecursive(
            courses = courses,
            candidatePairsByCourse = candidatePairsByCourse,
            constraints = constraints,
            index = 0,
            currentSessions = mutableListOf()
        )
    }

    /**
     * Filter session candidates based on preferred instructor/group constraints.
     */
    private fun filterSessionCandidates(
        sessions: List<Session>,
        courseId: java.util.UUID,
        type: SessionType,
        constraints: Constraints
    ): List<Session> {
        var filtered = sessions

        val preferredInstructor = constraints.preferredInstructors
            .find { it.courseId == courseId && it.type == type }
        if (preferredInstructor != null) {
            filtered = filtered.filter { it.instructor == preferredInstructor.instructor }
        }

        val preferredGroup = constraints.preferredGroups
            .find { it.courseId == courseId && it.type == type }
        if (preferredGroup != null) {
            filtered = filtered.filter { it.group == preferredGroup.group }
        }

        filtered = filtered.filter { session ->
            !constraints.excludedSessions.any { excluded ->
                excluded.courseId == courseId &&
                excluded.type == type &&
                excluded.group == session.group
            }
        }

        return filtered
    }

    /**
     * Recursively count valid combinations with early pruning.
     */
    private fun countValidCombinationsRecursive(
        courses: List<Course>,
        candidatePairsByCourse: Map<Course, List<Pair<Session, Session>>>,
        constraints: Constraints,
        index: Int,
        currentSessions: MutableList<Session>
    ): Int {
        if (index >= courses.size) {
            return 1
        }

        val course = courses[index]
        val candidatePairs = candidatePairsByCourse[course] ?: return 0

        var count = 0

        for ((lecture, section) in candidatePairs) {
            val newSessions = currentSessions + lecture + section

            if (hasTimeConflict(newSessions)) {
                continue
            }

            if (violatesMaxConstraints(newSessions, constraints)) {
                continue
            }

            count += countValidCombinationsRecursive(
                courses = courses,
                candidatePairsByCourse = candidatePairsByCourse,
                constraints = constraints,
                index = index + 1,
                currentSessions = newSessions.toMutableList()
            )
        }

        return count
    }

    /**
     * Check if any two sessions occupy the same time slot.
     */
    private fun hasTimeConflict(sessions: List<Session>): Boolean {
        val timeSlots = sessions.map { "${it.day.name}:${it.period}" }
        return timeSlots.size != timeSlots.distinct().size
    }

    /**
     * Check if max constraints are violated.
     */
    private fun violatesMaxConstraints(
        sessions: List<Session>,
        constraints: Constraints
    ): Boolean {
        constraints.weekLoadMax?.let { max ->
            val distinctDays = sessions.map { it.day }.distinct().size
            if (distinctDays > max) return true
        }

        constraints.dayLoadMax?.let { max ->
            val sessionsByDay = sessions.groupingBy { it.day }.eachCount()
            if (sessionsByDay.any { it.value > max }) return true
        }

        return false
    }
}
