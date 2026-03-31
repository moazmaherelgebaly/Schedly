package com.schedly.domain.model

/**
 * Input DTO for the generation engine.
 * Contains all courses with their sessions and the constraints to apply.
 */
data class GenerationRequest(
    val courses: List<Course>,
    val constraints: Constraints,
    val isRamadan: Boolean = false
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (courses.isEmpty()) {
            errors.add("At least one course is required")
        }

        courses.forEach { course ->
            if (course.lectures.isEmpty()) {
                errors.add("Course '${course.name}' must have at least one lecture")
            }
            if (course.sections.isEmpty()) {
                errors.add("Course '${course.name}' must have at least one section")
            }
        }

        // Validate constraints
        errors.addAll(constraints.validate())

        // Validate Ramadan flag (informational, not a validation error)
        if (isRamadan) {
            // Ramadan mode is enabled - this is for informational purposes
            // The actual period time mapping should use this flag in the UI layer
        }

        return errors
    }
}
