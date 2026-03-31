package com.schedly.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for Constraints domain model validation.
 * Tests all validation rules for constraint fields.
 */
class ConstraintsTest {

    @Test
    fun `validate returns empty errors for valid constraints`() {
        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            dayLoadMin = 1,
            dayLoadMax = 3,
            allowGaps = true,
            matchGroups = false
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate returns empty errors for empty constraints`() {
        val constraints = Constraints()

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate returns error when weekLoad min greater than max`() {
        val constraints = Constraints(
            weekLoadMin = 5,
            weekLoadMax = 3
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("week load", ignoreCase = true) })
        assertTrue(errors.any { it.contains("min") && it.contains("max") })
    }

    @Test
    fun `validate returns error when weekLoad out of range`() {
        val constraints = Constraints(
            weekLoadMin = 0,  // Invalid: must be 1-6
            weekLoadMax = 7   // Invalid: must be 1-6
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("week load", ignoreCase = true) })
        assertTrue(errors.any { it.contains("1 and 6") })
    }

    @Test
    fun `validate returns error when weekLoad min is zero`() {
        val constraints = Constraints(
            weekLoadMin = 0,
            weekLoadMax = 5
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("1 and 6") })
    }

    @Test
    fun `validate returns error when weekLoad max exceeds six`() {
        val constraints = Constraints(
            weekLoadMin = 1,
            weekLoadMax = 7
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("1 and 6") })
    }

    @Test
    fun `validate returns error when dayLoad min greater than max`() {
        val constraints = Constraints(
            dayLoadMin = 4,
            dayLoadMax = 2
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("day load", ignoreCase = true) })
        assertTrue(errors.any { it.contains("min") && it.contains("max") })
    }

    @Test
    fun `validate returns error when dayLoad out of range`() {
        val constraints = Constraints(
            dayLoadMin = -1,  // Invalid: must be 0-6
            dayLoadMax = 7    // Invalid: must be 0-6
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("day load", ignoreCase = true) })
        assertTrue(errors.any { it.contains("0 and 6") })
    }

    @Test
    fun `validate returns error when dayLoad min is negative`() {
        val constraints = Constraints(
            dayLoadMin = -1,
            dayLoadMax = 3
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("0 and 6") })
    }

    @Test
    fun `validate returns error when dayLoad max exceeds six`() {
        val constraints = Constraints(
            dayLoadMin = 1,
            dayLoadMax = 8
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("0 and 6") })
    }

    @Test
    fun `validate returns error when excludedDayPeriods has invalid period`() {
        val constraints = Constraints(
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 0)  // Invalid: must be 1-6
            )
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("period", ignoreCase = true) })
        assertTrue(errors.any { it.contains("1 and 6") })
    }

    @Test
    fun `validate returns error when excludedDayPeriods has period greater than six`() {
        val constraints = Constraints(
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SUNDAY, 7)  // Invalid: must be 1-6
            )
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("period", ignoreCase = true) })
    }

    @Test
    fun `validate returns multiple errors for multiple excludedDayPeriods violations`() {
        val constraints = Constraints(
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 0),
                DayPeriod(DayOfWeek.SUNDAY, 7),
                DayPeriod(DayOfWeek.MONDAY, 8)
            )
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertEquals(3, errors.size)
    }

    @Test
    fun `validate accepts valid excludedDayPeriods`() {
        val constraints = Constraints(
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 1),
                DayPeriod(DayOfWeek.SUNDAY, 3),
                DayPeriod(DayOfWeek.THURSDAY, 6)
            )
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts valid weekLoad with only min set`() {
        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = null
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts valid weekLoad with only max set`() {
        val constraints = Constraints(
            weekLoadMin = null,
            weekLoadMax = 5
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts valid dayLoad with only min set`() {
        val constraints = Constraints(
            dayLoadMin = 1,
            dayLoadMax = null
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts valid dayLoad with only max set`() {
        val constraints = Constraints(
            dayLoadMin = null,
            dayLoadMax = 3
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts constraints with only boolean flags set`() {
        val constraints = Constraints(
            allowGaps = false,
            matchGroups = true
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts constraints with preferredInstructors set`() {
        val courseId = UUID.randomUUID()
        val constraints = Constraints(
            preferredInstructors = listOf(
                InstructorPref(courseId, SessionType.LECTURE, "Dr. Smith")
            )
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts constraints with preferredGroups set`() {
        val courseId = UUID.randomUUID()
        val constraints = Constraints(
            preferredGroups = listOf(
                GroupPref(courseId, SessionType.SECTION, "G1")
            )
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts constraints with excludedSessions set`() {
        val courseId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        val constraints = Constraints(
            excludedSessions = listOf(
                SessionRef(courseId, sessionId)
            )
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate accepts all constraint types combined`() {
        val courseId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()

        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            dayLoadMin = 1,
            dayLoadMax = 3,
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 1),
                DayPeriod(DayOfWeek.THURSDAY, 6)
            ),
            excludedSessions = listOf(
                SessionRef(courseId, sessionId)
            ),
            allowGaps = false,
            matchGroups = true,
            preferredInstructors = listOf(
                InstructorPref(courseId, SessionType.LECTURE, "Dr. Smith")
            ),
            preferredGroups = listOf(
                GroupPref(courseId, SessionType.SECTION, "G1")
            )
        )

        val errors = constraints.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate returns combined errors for multiple violations`() {
        val constraints = Constraints(
            weekLoadMin = 5,
            weekLoadMax = 3,  // Error 1: min > max
            dayLoadMin = 4,
            dayLoadMax = 2,   // Error 2: min > max
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 0)  // Error 3: invalid period
            )
        )

        val errors = constraints.validate()

        assertFalse(errors.isEmpty())
        assertTrue(errors.size >= 3)
    }

    @Test
    fun `DayPeriod equals and hashCode work correctly`() {
        val dp1 = DayPeriod(DayOfWeek.SATURDAY, 1)
        val dp2 = DayPeriod(DayOfWeek.SATURDAY, 1)
        val dp3 = DayPeriod(DayOfWeek.SUNDAY, 1)

        assertEquals(dp1, dp2)
        assertEquals(dp1.hashCode(), dp2.hashCode())
        assertFalse(dp1 == dp3)
    }

    @Test
    fun `SessionRef equals and hashCode work correctly`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val sr1 = SessionRef(id1, id2)
        val sr2 = SessionRef(id1, id2)
        val sr3 = SessionRef(id2, id1)

        assertEquals(sr1, sr2)
        assertEquals(sr1.hashCode(), sr2.hashCode())
        assertFalse(sr1 == sr3)
    }

    @Test
    fun `InstructorPref equals and hashCode work correctly`() {
        val courseId = UUID.randomUUID()
        val ip1 = InstructorPref(courseId, SessionType.LECTURE, "Dr. Smith")
        val ip2 = InstructorPref(courseId, SessionType.LECTURE, "Dr. Smith")
        val ip3 = InstructorPref(courseId, SessionType.SECTION, "Dr. Smith")

        assertEquals(ip1, ip2)
        assertEquals(ip1.hashCode(), ip2.hashCode())
        assertFalse(ip1 == ip3)
    }

    @Test
    fun `GroupPref equals and hashCode work correctly`() {
        val courseId = UUID.randomUUID()
        val gp1 = GroupPref(courseId, SessionType.SECTION, "G1")
        val gp2 = GroupPref(courseId, SessionType.SECTION, "G1")
        val gp3 = GroupPref(courseId, SessionType.SECTION, "G2")

        assertEquals(gp1, gp2)
        assertEquals(gp1.hashCode(), gp2.hashCode())
        assertFalse(gp1 == gp3)
    }
}
