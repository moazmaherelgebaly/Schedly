package com.schedly.domain.repository

import com.schedly.domain.model.Constraints
import com.schedly.domain.model.Course

/**
 * Repository interface for Course data operations.
 * Abstracts data source (Room, in-memory, etc.) for KMP readiness.
 */
interface CourseRepository {
    
    /**
     * Get all courses with their sessions.
     */
    suspend fun getAllCourses(): List<Course>
    
    /**
     * Get a specific course by ID.
     */
    suspend fun getCourseById(id: java.util.UUID): Course?
    
    /**
     * Insert or update a course.
     */
    suspend fun saveCourse(course: Course)
    
    /**
     * Delete a course by ID.
     */
    suspend fun deleteCourse(id: java.util.UUID)
    
    /**
     * Check if any courses exist.
     */
    suspend fun hasCourses(): Boolean
}
