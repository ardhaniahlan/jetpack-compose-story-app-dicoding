package org.apps.composestoryapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
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
import javax.inject.Singleton

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
}