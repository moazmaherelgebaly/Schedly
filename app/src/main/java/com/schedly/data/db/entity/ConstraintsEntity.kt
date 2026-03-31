package com.schedly.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.schedly.data.db.ConstraintsConverter
import com.schedly.domain.model.Constraints

@Entity(tableName = "constraints")
@TypeConverters(ConstraintsConverter::class)
data class ConstraintsEntity(
    @PrimaryKey
    val id: Int = 1,
    val jsonData: String
) {
    fun toDomain(): Constraints = ConstraintsConverter().toConstraints(jsonData)

    companion object {
        fun fromDomain(constraints: Constraints): ConstraintsEntity =
            ConstraintsEntity(id = 1, jsonData = ConstraintsConverter().fromConstraints(constraints))
    }
}
