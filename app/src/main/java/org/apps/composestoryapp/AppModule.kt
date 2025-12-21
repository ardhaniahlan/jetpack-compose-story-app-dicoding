package org.apps.composestoryapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.apps.composestoryapp.remote.RetrofitClient
import org.apps.composestoryapp.remote.SessionManager
import org.apps.composestoryapp.remote.StoryApiService
import org.apps.composestoryapp.repository.AuthRepository
import org.apps.composestoryapp.repository.AuthRepositoryImpl
import org.apps.composestoryapp.repository.StoryRepository
import org.apps.composestoryapp.repository.StoryRepositoryImpl
import org.apps.composestoryapp.room.FavoriteDatabase
import org.apps.composestoryapp.room.StoryFavoriteDao
import org.apps.composestoryapp.room.StoryFavoriteRepository
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesDatastore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("session") }
        )

    @Provides
    @Singleton
    fun providesSessionManager(dataStore: DataStore<Preferences>): SessionManager =
        SessionManager(dataStore)

    @Provides
    @Singleton
    fun provideStoryApiService(sessionManager: SessionManager): StoryApiService =
        RetrofitClient.createApi(sessionManager)

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: StoryApiService,
        sessionManager: SessionManager
    ): AuthRepository =
        AuthRepositoryImpl(apiService, sessionManager)

    @Provides
    @Singleton
    fun provideStoryRepository(
        apiService: StoryApiService,
        @ApplicationContext context: Context
    ): StoryRepository =
        StoryRepositoryImpl(apiService, context)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FavoriteDatabase {
        return Room.databaseBuilder(
            context,
            FavoriteDatabase::class.java,
            "story_favorite_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideStoryFavoriteDao(db: FavoriteDatabase): StoryFavoriteDao {
        return db.storyFavoriteDao()
    }

    @Provides
    @Singleton
    fun provideStoryFavoriteRepository(
        dao: StoryFavoriteDao
    ): StoryFavoriteRepository {
        return StoryFavoriteRepository(dao)
    }
}