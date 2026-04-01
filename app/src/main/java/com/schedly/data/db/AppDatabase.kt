package com.schedly.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.schedly.app.BuildConfig
import com.schedly.data.db.dao.ConstraintsDao
import com.schedly.data.db.dao.CourseDao
import com.schedly.data.db.dao.SessionDao
import com.schedly.data.db.entity.ConstraintsEntity
import com.schedly.data.db.entity.CourseEntity
import com.schedly.data.db.entity.SessionEntity

@Database(
    entities = [CourseEntity::class, SessionEntity::class, ConstraintsEntity::class],
    version = 2,
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate constraints table with CHECK constraint to enforce singleton row (id = 1)
                db.execSQL("CREATE TABLE IF NOT EXISTS `constraints_backup` (`id` INTEGER NOT NULL, `jsonData` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `constraints_backup` SELECT * FROM `constraints`")
                db.execSQL("DROP TABLE `constraints`")
                db.execSQL("CREATE TABLE `constraints` (`id` INTEGER NOT NULL CHECK(id = 1), `jsonData` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `constraints` SELECT * FROM `constraints_backup`")
                db.execSQL("DROP TABLE `constraints_backup`")
            }
        }

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

                    builder.addMigrations(MIGRATION_1_2)

                    val instance = builder.build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
}
