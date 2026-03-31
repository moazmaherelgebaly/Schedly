package com.schedly.domain.repository

import com.schedly.domain.model.Constraints

/**
 * Repository interface for Constraints data operations.
 * Abstracts data source (DataStore, Room, etc.) for KMP readiness.
 */
interface ConstraintsRepository {
    
    /**
     * Get the current constraints.
     * Returns default constraints if none are saved.
     */
    suspend fun getConstraints(): Constraints
    
    /**
     * Save constraints.
     */
    suspend fun saveConstraints(constraints: Constraints)
    
    /**
     * Reset constraints to default values.
     */
    suspend fun resetToDefaults()
    
    /**
     * Check if custom constraints have been saved.
     */
    suspend fun hasCustomConstraints(): Boolean
}
