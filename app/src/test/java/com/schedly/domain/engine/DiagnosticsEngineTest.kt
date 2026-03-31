package com.schedly.domain.engine

import com.schedly.domain.model.*
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.util.UUID

class DiagnosticsEngineTest {
    
    private val diagnosticsEngine = DiagnosticsEngine()
    
    private fun createCourse(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Course",
        lectures: List<Session> = emptyList(),
        sections: List<Session> = emptyList()
    ): Course {
        return Course(id = id, name = name, lectures = lectures, sections = sections)
    }
    
    private fun createSession(
        courseId: UUID,
        type: SessionType,
        group: String = "G1",
        day: DayOfWeek = DayOfWeek.SATURDAY,
        period: Int = 1
    ): Session {
        return Session(
            courseId = courseId,
            type = type,
            group = group,
            instructor = "Dr. Test",
            day = day,
            period = period,
            room = "Room 101",
            priority = PriorityLevel.MEDIUM
        )
    }
    
    @Test
    fun `test diagnose zero baseline`() {
        val courses = emptyList<Course>()
        val constraints = Constraints()
        
        val diagnostic = diagnosticsEngine.diagnose(courses, constraints, baselineCount = 0)
        
        assertEquals("Input Data", diagnostic.mostRestrictiveConstraint)
        assertEquals(0, diagnostic.baselineCount)
        assertTrue(diagnostic.hint.isNotEmpty())
    }
    
    @Test
    fun `test diagnose with exclude all days constraint`() {
        val courseId = UUID.randomUUID()
        val course = createCourse(
            id = courseId,
            lectures = listOf(
                createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1)
            ),
            sections = listOf(
                createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
            )
        )
        
        // Exclude all day-periods where sessions exist
        val constraints = Constraints(
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 1),
                DayPeriod(DayOfWeek.SATURDAY, 2)
            )
        )
        
        val diagnostic = diagnosticsEngine.diagnose(
            courses = listOf(course),
            constraints = constraints,
            baselineCount = 1
        )
        
        assertNotNull(diagnostic)
        assertTrue(diagnostic.constraintImpacts.isNotEmpty())
        assertTrue(diagnostic.hint.contains("eliminating all candidates"))
    }
    
    @Test
    fun `test diagnose identifies most restrictive constraint`() {
        val courseId = UUID.randomUUID()
        val course = createCourse(
            id = courseId,
            lectures = listOf(
                createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
                createSession(courseId, SessionType.LECTURE, "G2", DayOfWeek.SUNDAY, 1)
            ),
            sections = listOf(
                createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2),
                createSession(courseId, SessionType.SECTION, "G2", DayOfWeek.SUNDAY, 2)
            )
        )
        
        // Multiple constraints, one should be most restrictive
        val constraints = Constraints(
            weekLoadMax = 1, // Very restrictive - only 1 day allowed
            excludedDayPeriods = listOf(DayPeriod(DayOfWeek.MONDAY, 1)) // Less restrictive
        )
        
        val diagnostic = diagnosticsEngine.diagnose(
            courses = listOf(course),
            constraints = constraints,
            baselineCount = 4
        )
        
        assertNotNull(diagnostic)
        // Week Load should be identified as most restrictive
        assertTrue(diagnostic.constraintImpacts.containsKey("Week Load"))
    }
    
    @Test
    fun `test smart hint generation`() {
        val courseId = UUID.randomUUID()
        val course = createCourse(
            id = courseId,
            lectures = listOf(
                createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1)
            ),
            sections = listOf(
                createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
            )
        )
        
        val constraints = Constraints(
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 1),
                DayPeriod(DayOfWeek.SATURDAY, 2)
            )
        )
        
        val diagnostic = diagnosticsEngine.diagnose(
            courses = listOf(course),
            constraints = constraints,
            baselineCount = 1
        )
        
        assertTrue(diagnostic.hint.startsWith("No schedules found"))
        assertTrue(diagnostic.hint.contains("try relaxing"))
    }
    
    @Test
    fun `test constraint impacts map populated`() {
        val courseId = UUID.randomUUID()
        val course = createCourse(
            id = courseId,
            lectures = listOf(
                createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1)
            ),
            sections = listOf(
                createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SUNDAY, 1)
            )
        )
        
        val constraints = Constraints(
            weekLoadMax = 1,
            dayLoadMax = 1,
            allowGaps = false
        )
        
        val diagnostic = diagnosticsEngine.diagnose(
            courses = listOf(course),
            constraints = constraints,
            baselineCount = 1
        )
        
        // Should have impact for each active constraint
        assertTrue(diagnostic.constraintImpacts.isNotEmpty())
    }
}
