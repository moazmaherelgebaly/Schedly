package com.schedly.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.schedly.app.BuildConfig
import com.schedly.data.db.dao.ConstraintsDao
import com.schedly.data.db.dao.CourseDao
import com.schedly.data.db.dao.SessionDao
import com.schedly.data.db.entity.ConstraintsEntity
import com.schedly.data.db.entity.CourseEntity
import com.schedly.data.db.entity.SessionEntity

@Database(
    entities = [CourseEntity::class, SessionEntity::class, ConstraintsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class, ConstraintsConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun sessionDao(): SessionDao
    abstract fun constraintsDao(): ConstraintsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val builder = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "schedly_database"
                    )

                    // Only allow destructive downgrade in debug builds
                    if (BuildConfig.ENABLE_DESTRUCTIVE_DOWNGRADE) {
                        builder.fallbackToDestructiveMigrationOnDowngrade()
                    }

                    val instance = builder.build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
}
