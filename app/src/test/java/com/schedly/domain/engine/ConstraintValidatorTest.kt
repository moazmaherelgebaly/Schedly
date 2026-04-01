package com.schedly.domain.engine

import com.schedly.domain.model.*
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import java.util.UUID

class ConstraintValidatorTest {
    
    private val validator = ConstraintValidator()
    
    private fun createSession(
        courseId: UUID = UUID.randomUUID(),
        type: SessionType = SessionType.LECTURE,
        group: String = "G1",
        day: DayOfWeek = DayOfWeek.SATURDAY,
        period: Int = 1,
        instructor: String = "Dr. Test"
    ): Session {
        return Session(
            courseId = courseId,
            type = type,
            group = group,
            instructor = instructor,
            day = day,
            period = period,
            room = "Room 101",
            priority = PriorityLevel.MEDIUM
        )
    }
    
    private fun createSchedule(sessions: List<Session>): Schedule {
        return Schedule.create(sessions)
    }
    
    @Test
    fun `test excluded day-period constraint - pass`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            excludedDayPeriods = listOf(DayPeriod(DayOfWeek.SUNDAY, 1))
        )
        
        assertTrue(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test excluded day-period constraint - fail`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            excludedDayPeriods = listOf(DayPeriod(DayOfWeek.SATURDAY, 1))
        )
        
        assertFalse(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test week load constraint - pass`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SUNDAY, 2)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            weekLoadMin = 2,
            weekLoadMax = 3
        )
        
        assertTrue(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test week load constraint - fail max`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SUNDAY, 2),
            createSession(courseId, SessionType.LECTURE, "G2", DayOfWeek.MONDAY, 3)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            weekLoadMax = 2
        )
        
        assertFalse(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test day load constraint - pass`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            dayLoadMax = 3
        )
        
        assertTrue(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test day load constraint - fail max`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2),
            createSession(courseId, SessionType.LECTURE, "G2", DayOfWeek.SATURDAY, 3),
            createSession(courseId, SessionType.SECTION, "G2", DayOfWeek.SATURDAY, 4)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            dayLoadMax = 3
        )
        
        assertFalse(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test allow gaps constraint - pass with gaps`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 3) // Gap at period 2
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            allowGaps = true
        )
        
        assertTrue(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test allow gaps constraint - fail no gaps allowed`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 3) // Gap at period 2
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            allowGaps = false
        )
        
        assertFalse(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test allow gaps constraint - pass no gaps`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2) // No gap
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            allowGaps = false
        )
        
        assertTrue(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test excluded sessions constraint - pass`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            excludedSessions = listOf(
                SessionRef(courseId, SessionType.LECTURE, "G2") // Different group
            )
        )
        
        assertTrue(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test excluded sessions constraint - fail`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            excludedSessions = listOf(
                SessionRef(courseId, SessionType.LECTURE, "G1") // Same group
            )
        )
        
        assertFalse(validator.validate(schedule, constraints))
    }
    
    @Test
    fun `test preferred instructor constraint - pass`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(
                courseId = courseId,
                type = SessionType.LECTURE,
                group = "G1",
                instructor = "Dr. Preferred"
            ),
            createSession(courseId, SessionType.SECTION, "G1", instructor = "Dr. Other")
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            preferredInstructors = listOf(
                InstructorPref(courseId, SessionType.LECTURE, "Dr. Preferred")
            )
        )
        
        assertTrue(validator.checkPreferredInstructors(schedule, constraints))
    }
    
    @Test
    fun `test preferred instructor constraint - fail`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(
                courseId = courseId,
                type = SessionType.LECTURE,
                group = "G1",
                instructor = "Dr. Wrong"
            ),
            createSession(courseId, SessionType.SECTION, "G1", instructor = "Dr. Other")
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            preferredInstructors = listOf(
                InstructorPref(courseId, SessionType.LECTURE, "Dr. Preferred")
            )
        )
        
        assertFalse(validator.checkPreferredInstructors(schedule, constraints))
    }
    
    @Test
    fun `test preferred group constraint - pass`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1"),
            createSession(courseId, SessionType.SECTION, "G1")
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            preferredGroups = listOf(
                GroupPref(courseId, SessionType.LECTURE, "G1")
            )
        )
        
        assertTrue(validator.checkPreferredGroups(schedule, constraints))
    }
    
    @Test
    fun `test preferred group constraint - fail`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G2"),
            createSession(courseId, SessionType.SECTION, "G2")
        )
        val schedule = createSchedule(sessions)
        
        val constraints = Constraints(
            preferredGroups = listOf(
                GroupPref(courseId, SessionType.LECTURE, "G1")
            )
        )
        
        assertFalse(validator.checkPreferredGroups(schedule, constraints))
    }

    @Test
    fun `test match groups constraint - pass with matching groups`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)

        val constraints = Constraints(
            matchGroups = true
        )

        assertTrue(validator.checkMatchGroups(schedule, constraints))
    }

    @Test
    fun `test match groups constraint - fail with mismatched groups`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G2", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)

        val constraints = Constraints(
            matchGroups = true
        )

        assertFalse(validator.checkMatchGroups(schedule, constraints))
    }

    @Test
    fun `test match groups constraint - pass when disabled`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.SECTION, "G2", DayOfWeek.SATURDAY, 2)
        )
        val schedule = createSchedule(sessions)

        val constraints = Constraints(
            matchGroups = false
        )

        assertTrue(validator.checkMatchGroups(schedule, constraints))
    }

    @Test
    fun `test match groups constraint - pass with multiple matching pairs`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.LECTURE, "G2", DayOfWeek.SUNDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2),
            createSession(courseId, SessionType.SECTION, "G2", DayOfWeek.SUNDAY, 2)
        )
        val schedule = createSchedule(sessions)

        val constraints = Constraints(
            matchGroups = true
        )

        assertTrue(validator.checkMatchGroups(schedule, constraints))
    }

    @Test
    fun `test match groups constraint - fail with extra unmatched group`() {
        val courseId = UUID.randomUUID()
        val sessions = listOf(
            createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
            createSession(courseId, SessionType.LECTURE, "G2", DayOfWeek.SUNDAY, 1),
            createSession(courseId, SessionType.LECTURE, "G3", DayOfWeek.MONDAY, 1),
            createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2),
            createSession(courseId, SessionType.SECTION, "G2", DayOfWeek.SUNDAY, 2)
        )
        val schedule = createSchedule(sessions)

        val constraints = Constraints(
            matchGroups = true
        )

        assertFalse(validator.checkMatchGroups(schedule, constraints))
    }
}
