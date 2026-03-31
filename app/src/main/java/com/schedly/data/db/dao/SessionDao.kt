package com.schedly.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.schedly.data.db.entity.SessionEntity

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions")
    suspend fun getAll(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE courseId = :courseId")
    suspend fun getSessionsForCourse(courseId: String): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE courseId = :courseId")
    suspend fun deleteSessionsForCourse(courseId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM sessions WHERE day = :day AND period = :period AND room = :room AND (:excludeId IS NULL OR id != :excludeId))")
    suspend fun isRoomOccupied(day: String, period: Int, room: String, excludeId: String? = null): Boolean
}
