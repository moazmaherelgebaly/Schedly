package com.schedly.domain.repository

/**
 * Domain-level abstraction for preferences operations.
 * This interface lives in the domain layer to maintain clean architecture boundaries.
 */
interface PreferencesRepository {
    suspend fun getRamadanOffset(): Int
    suspend fun setRamadanOffset(offset: Int)
}
