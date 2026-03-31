package com.schedly.di

import android.content.Context
import com.schedly.data.datastore.PreferencesManager
import com.schedly.data.datastore.dataStore
import com.schedly.data.db.AppDatabase
import com.schedly.data.db.dao.ConstraintsDao
import com.schedly.data.db.dao.CourseDao
import com.schedly.data.db.dao.SessionDao
import com.schedly.data.repository.ConstraintsRepositoryImpl
import com.schedly.data.repository.CourseRepositoryImpl
import com.schedly.domain.repository.IConstraintsRepository
import com.schedly.domain.repository.ICourseRepository
import com.schedly.domain.ramadan.RamadanDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCourseDao(database: AppDatabase) = database.courseDao()

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase) = database.sessionDao()

    @Provides
    @Singleton
    fun provideConstraintsDao(database: AppDatabase) = database.constraintsDao()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideRamadanDetector(preferencesManager: PreferencesManager): RamadanDetector {
        return RamadanDetector(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(
        courseDao: CourseDao,
        sessionDao: SessionDao,
        database: AppDatabase
    ): ICourseRepository {
        return CourseRepositoryImpl(courseDao, sessionDao, database)
    }

    @Provides
    @Singleton
    fun provideConstraintsRepository(
        constraintsDao: ConstraintsDao
    ): IConstraintsRepository {
        return ConstraintsRepositoryImpl(constraintsDao)
    }
}
