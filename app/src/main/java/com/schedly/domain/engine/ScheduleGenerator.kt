package com.schedly.domain.engine

import com.schedly.domain.model.*
import java.util.UUID

/**
 * Main schedule generation engine.
 * Implements brute-force Cartesian product with early pruning.
 */
class ScheduleGenerator {
    
    private val validator = ConstraintValidator()
    private val priorityDropLogic = PriorityDropLogic()
    private val diagnosticsEngine = DiagnosticsEngine()
    
    /**
     * Generate all valid schedules from courses and constraints.
     * 
     * @param request The generation request containing courses and constraints
     * @return GenerationResult with all valid schedules (may be empty)
     */
    fun generate(request: GenerationRequest): GenerationResult {
        // Validate input
        val errors = request.validate()
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException("Invalid generation request: ${errors.joinToString(", ")}")
        }
        
        val courses = request.courses
        val constraints = request.constraints
        
        // Build candidate pairs for each course
        val candidatePairsByCourse = buildCandidatePairs(courses, constraints)
        
        // Check if any course has no candidates
        if (candidatePairsByCourse.any { it.value.isEmpty() }) {
            val diagnostic = diagnosticsEngine.diagnose(
                courses = courses,
                constraints = constraints,
                baselineCount = 0,
                generator = this
            )
            return GenerationResult.zeroResults(diagnostic)
        }
        
        // Generate all combinations with early pruning
        val allSchedules = generateCombinations(
            courses = courses,
            candidatePairsByCourse = candidatePairsByCourse,
            constraints = constraints
        )
        
        // Apply priority drop logic to recover partial schedules
        val finalSchedules = allSchedules.mapNotNull { schedule ->
            if (schedule.hasConflicts() || !validator.validate(schedule, constraints)) {
                priorityDropLogic.apply(schedule, constraints)
            } else {
                schedule
            }
        }
        
        // Handle zero results
        if (finalSchedules.isEmpty()) {
            val baselineCount = countBaseline(courses, constraints)
            val diagnostic = diagnosticsEngine.diagnose(
                courses = courses,
                constraints = constraints,
                baselineCount = baselineCount,
                generator = this
            )
            return GenerationResult.zeroResults(diagnostic)
        }
        
        // Sort by week load (ascending), then day load (ascending)
        val sortedSchedules = finalSchedules.sortedWith(
            compareBy({ it.weekLoad }, { it.dayLoad.values.sum() })
        )
        
        return GenerationResult.success(sortedSchedules)
    }
    
    /**
     * Build candidate pairs (lecture + section) for each course.
     * Applies preferred instructor/group filters immediately.
     */
    private fun buildCandidatePairs(
        courses: List<Course>,
        constraints: Constraints
    ): Map<Course, List<Pair<Session, Session>>> {
        return courses.associate { course ->
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

            // Build pairs
            val pairs = mutableListOf<Pair<Session, Session>>()
            for (lecture in lectureCandidates) {
                for (section in sectionCandidates) {
                    // If matchGroups is enabled, only pair same group
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
    }
    
    /**
     * Filter session candidates based on preferred instructor/group constraints.
     */
    private fun filterSessionCandidates(
        sessions: List<Session>,
        courseId: UUID,
        type: SessionType,
        constraints: Constraints
    ): List<Session> {
        var filtered = sessions
        
        // Apply preferred instructor filter
        val preferredInstructor = constraints.preferredInstructors
            .find { it.courseId == courseId && it.type == type }
        if (preferredInstructor != null) {
            filtered = filtered.filter { it.instructor == preferredInstructor.instructor }
        }
        
        // Apply preferred group filter
        val preferredGroup = constraints.preferredGroups
            .find { it.courseId == courseId && it.type == type }
        if (preferredGroup != null) {
            filtered = filtered.filter { it.group == preferredGroup.group }
        }
        
        // Apply excluded sessions filter
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
     * Generate all combinations using Cartesian product with early pruning.
     */
    private fun generateCombinations(
        courses: List<Course>,
        candidatePairsByCourse: Map<Course, List<Pair<Session, Session>>>,
        constraints: Constraints
    ): List<Schedule> {
        val courseList = courses.toList()
        return generateRecursive(
            courses = courseList,
            candidatePairsByCourse = candidatePairsByCourse,
            constraints = constraints,
            index = 0,
            currentSessions = mutableListOf()
        )
    }

    private fun generateRecursive(
        courses: List<Course>,
        candidatePairsByCourse: Map<Course, List<Pair<Session, Session>>>,
        constraints: Constraints,
        index: Int,
        currentSessions: MutableList<Session>
    ): List<Schedule> {
        // Base case: all courses processed
        if (index >= courses.size) {
            return listOf(Schedule.create(currentSessions.toList()))
        }

        val course = courses[index]
        val candidatePairs = candidatePairsByCourse[course] ?: return emptyList()

        val results = mutableListOf<Schedule>()

        for ((lecture, section) in candidatePairs) {
            val newSessions = currentSessions + lecture + section

            // Early pruning: check for time conflicts
            if (hasTimeConflict(newSessions)) {
                continue
            }

            // Early pruning: check max constraints
            if (violatesMaxConstraints(newSessions, constraints)) {
                continue
            }

            // Recurse
            results.addAll(
                generateRecursive(
                    courses = courses,
                    candidatePairsByCourse = candidatePairsByCourse,
                    constraints = constraints,
                    index = index + 1,
                    currentSessions = newSessions.toMutableList()
                )
            )
        }

        return results
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
        // Check week load max
        constraints.weekLoadMax?.let { max ->
            val distinctDays = sessions.map { it.day }.distinct().size
            if (distinctDays > max) return true
        }
        
        // Check day load max
        constraints.dayLoadMax?.let { max ->
            val sessionsByDay = sessions.groupingBy { it.day }.eachCount()
            if (sessionsByDay.any { it.value > max }) return true
        }
        
        return false
    }
    
    /**
     * Count baseline schedules with no constraints for diagnostics.
     */
    private fun countBaseline(courses: List<Course>, constraints: Constraints): Int {
        val noConstraints = constraints.copy(
            weekLoadMin = null,
            weekLoadMax = null,
            dayLoadMin = null,
            dayLoadMax = null,
            excludedDayPeriods = emptyList(),
            excludedSessions = emptyList(),
            allowGaps = true,
            matchGroups = false,
            preferredInstructors = emptyList(),
            preferredGroups = emptyList()
        )
        
        val candidatePairsByCourse = buildCandidatePairs(courses, noConstraints)
        return generateCombinations(courses, candidatePairsByCourse, noConstraints).size
    }
}
