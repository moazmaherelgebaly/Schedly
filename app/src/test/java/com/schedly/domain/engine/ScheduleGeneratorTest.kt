package com.schedly.domain.engine

import com.schedly.domain.model.*
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import java.util.UUID

class ScheduleGeneratorTest {
    
    private val generator = ScheduleGenerator()
    
    // Helper to create test data
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
        period: Int = 1,
        priority: PriorityLevel = PriorityLevel.MEDIUM
    ): Session {
        return Session(
            courseId = courseId,
            type = type,
            group = group,
            instructor = "Dr. Test",
            day = day,
            period = period,
            room = "Room 101",
            priority = priority
        )
    }
    
    @Test
    fun `test conflict detection - no conflicts`() {
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
        
        val request = GenerationRequest(
            courses = listOf(course),
            constraints = Constraints()
        )
        
        val result = generator.generate(request)
        
        assertTrue(result.schedules.isNotEmpty())
        assertTrue(result.schedules.all { !it.hasConflicts() })
    }
    
    @Test
    fun `test conflict detection - with conflicts`() {
        val courseId = UUID.randomUUID()
        // Create sessions at the same time - should be filtered out
        val course = createCourse(
            id = courseId,
            lectures = listOf(
                createSession(courseId, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
                createSession(courseId, SessionType.LECTURE, "G2", DayOfWeek.SATURDAY, 1) // Same time
            ),
            sections = listOf(
                createSession(courseId, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2)
            )
        )
        
        val request = GenerationRequest(
            courses = listOf(course),
            constraints = Constraints()
        )
        
        val result = generator.generate(request)
        
        // Should still generate valid schedules (picking non-conflicting sessions)
        assertTrue(result.schedules.isNotEmpty())
        assertTrue(result.schedules.all { !it.hasConflicts() })
    }
    
    @Test
    fun `test Cartesian product - multiple courses`() {
        val course1Id = UUID.randomUUID()
        val course2Id = UUID.randomUUID()
        
        val course1 = createCourse(
            id = course1Id,
            lectures = listOf(
                createSession(course1Id, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
                createSession(course1Id, SessionType.LECTURE, "G2", DayOfWeek.SUNDAY, 1)
            ),
            sections = listOf(
                createSession(course1Id, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 2),
                createSession(course1Id, SessionType.SECTION, "G2", DayOfWeek.SUNDAY, 2)
            )
        )
        
        val course2 = createCourse(
            id = course2Id,
            lectures = listOf(
                createSession(course2Id, SessionType.LECTURE, "G1", DayOfWeek.MONDAY, 1)
            ),
            sections = listOf(
                createSession(course2Id, SessionType.SECTION, "G1", DayOfWeek.MONDAY, 2)
            )
        )
        
        val request = GenerationRequest(
            courses = listOf(course1, course2),
            constraints = Constraints()
        )
        
        val result = generator.generate(request)
        
        // Should generate 4 schedules (2 lecture × 2 section for course1 × 1 for course2)
        assertEquals(4, result.schedules.size)
    }
    
    @Test
    fun `test performance - worst case 1024 candidates`() {
        // Create 5 courses with 4 lecture groups and 4 section groups each
        val dayCount = DayOfWeek.values().size
        val periodCount = 6

        val courses = (1..5).map { courseNum ->
            val courseId = UUID.randomUUID()
            val lectures = (1..4).map { groupNum ->
                createSession(
                    courseId = courseId,
                    type = SessionType.LECTURE,
                    group = "G$groupNum",
                    day = DayOfWeek.values()[(courseNum + groupNum) % dayCount],
                    period = ((courseNum * groupNum) % periodCount) + 1
                )
            }
            val sections = (1..4).map { groupNum ->
                createSession(
                    courseId = courseId,
                    type = SessionType.SECTION,
                    group = "G$groupNum",
                    day = DayOfWeek.values()[(courseNum + groupNum + 1) % dayCount],
                    period = ((courseNum * groupNum + 2) % periodCount) + 1
                )
            }
            createCourse(id = courseId, name = "Course $courseNum", lectures = lectures, sections = sections)
        }
        
        val request = GenerationRequest(
            courses = courses,
            constraints = Constraints()
        )
        
        val startTime = System.currentTimeMillis()
        val result = generator.generate(request)
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue("Generation took ${elapsed}ms, expected <2000ms", elapsed < 2000)
        assertTrue(result.schedules.isNotEmpty())
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test empty courses list`() {
        val request = GenerationRequest(
            courses = emptyList(),
            constraints = Constraints()
        )
        
        generator.generate(request)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test course without lectures`() {
        val courseId = UUID.randomUUID()
        val course = createCourse(
            id = courseId,
            lectures = emptyList(),
            sections = listOf(
                createSession(courseId, SessionType.SECTION, "G1")
            )
        )
        
        val request = GenerationRequest(
            courses = listOf(course),
            constraints = Constraints()
        )
        
        generator.generate(request)
    }
    
    @Test
    fun `test precomputed weekLoad and dayLoad`() {
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
        
        val request = GenerationRequest(
            courses = listOf(course),
            constraints = Constraints()
        )
        
        val result = generator.generate(request)
        
        assertTrue(result.schedules.isNotEmpty())
        result.schedules.forEach { schedule ->
            assertEquals(schedule.sessions.map { it.day }.distinct().size, schedule.weekLoad)
            assertEquals(schedule.sessions.groupingBy { it.day }.eachCount(), schedule.dayLoad)
        }
    }
    
    @Test
    fun `test default sorting by week load`() {
        val course1Id = UUID.randomUUID()
        val course2Id = UUID.randomUUID()
        
        // Create courses that will generate schedules with different week loads
        val course1 = createCourse(
            id = course1Id,
            lectures = listOf(
                createSession(course1Id, SessionType.LECTURE, "G1", DayOfWeek.SATURDAY, 1),
                createSession(course1Id, SessionType.LECTURE, "G2", DayOfWeek.SATURDAY, 2) // Same day
            ),
            sections = listOf(
                createSession(course1Id, SessionType.SECTION, "G1", DayOfWeek.SATURDAY, 3),
                createSession(course1Id, SessionType.SECTION, "G2", DayOfWeek.SUNDAY, 1)
            )
        )
        
        val course2 = createCourse(
            id = course2Id,
            lectures = listOf(
                createSession(course2Id, SessionType.LECTURE, "G1", DayOfWeek.MONDAY, 1)
            ),
            sections = listOf(
                createSession(course2Id, SessionType.SECTION, "G1", DayOfWeek.MONDAY, 2)
            )
        )
        
        val request = GenerationRequest(
            courses = listOf(course1, course2),
            constraints = Constraints()
        )
        
        val result = generator.generate(request)
        
        assertTrue(result.schedules.isNotEmpty())
        // Verify sorted by week load ascending
        val weekLoads = result.schedules.map { it.weekLoad }
        assertEquals(weekLoads.sorted(), weekLoads)
    }
}
