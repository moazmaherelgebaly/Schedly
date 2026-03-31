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
        generator: ScheduleGenerator? = null
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
     */
    private fun testWithSingleConstraint(
        courses: List<Course>,
        baseConstraints: Constraints,
        constraintName: String,
        testConstraints: Constraints,
        generator: ScheduleGenerator?
    ): Int {
        // Use provided generator or create a new one (only for top-level calls)
        return try {
            if (generator != null) {
                val request = GenerationRequest(courses, testConstraints)
                val result = generator.generate(request)
                result.schedules.size
            } else {
                // For recursive calls, use a simple counting method
                countCandidates(courses, testConstraints)
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Simple candidate counting without full generation (for diagnostics only).
     */
    private fun countCandidates(courses: List<Course>, constraints: Constraints): Int {
        // Simplified counting - just count possible combinations without validation
        var count = 1
        for (course in courses) {
            val lectureCount = course.lectures.size
            val sectionCount = course.sections.size
            if (lectureCount == 0 || sectionCount == 0) return 0
            count *= lectureCount * sectionCount
        }
        return count
    }
}
