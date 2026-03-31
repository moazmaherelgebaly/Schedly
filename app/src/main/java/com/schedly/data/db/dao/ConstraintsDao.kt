package com.schedly.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.schedly.data.db.entity.ConstraintsEntity

@Dao
interface ConstraintsDao {

    @Query("SELECT * FROM constraints WHERE id = 1")
    suspend fun getConstraints(): ConstraintsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConstraints(constraints: ConstraintsEntity)

    @Query("DELETE FROM constraints WHERE id = 1")
    suspend fun deleteConstraints()
}
