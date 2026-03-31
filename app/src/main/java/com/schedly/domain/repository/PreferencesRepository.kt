package com.schedly.domain.repository

/**
 * Repository interface for user preferences operations.
 * 
 * This interface provides an abstraction for storing and retrieving user
 * preferences using DataStore Preferences. It lives in the domain layer
 * to maintain clean architecture boundaries and enable KMP migration.
 * 
 * Current preferences:
 * - Ramadan offset (±1 day adjustment for Hijri calendar detection)
 * 
 * Future preferences may include:
 * - Last viewed schedule
 * - Sort preferences
 * - Theme settings
 */
interface PreferencesRepository {
    /**
     * Get the Ramadan detection offset.
     *
     * This hidden developer offset (±1 day) adjusts the automatic Ramadan
     * detection to account for moon sighting variations. Not exposed in UI.
     *
     * @return Offset in days (typically -1, 0, or 1)
     */
    suspend fun getRamadanOffset(): Int

    /**
     * Set the Ramadan detection offset.
     *
     * @param offset The offset in days (-1, 0, or 1)
     */
    suspend fun setRamadanOffset(offset: Int)
}
