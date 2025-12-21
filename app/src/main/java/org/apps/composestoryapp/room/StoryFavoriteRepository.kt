package org.apps.composestoryapp.room

import kotlinx.coroutines.flow.Flow
import org.apps.composestoryapp.model.Story

class StoryFavoriteRepository(
    private val dao: StoryFavoriteDao
) {
    fun isFavorite(storyId: String): Flow<Boolean> =
        dao.isFavorite(storyId)

    fun getFavorites(): Flow<List<StoryFavoriteEntity>> =
        dao.getFavorites()

    suspend fun toggleFavorite(story: Story) {
        val entity = StoryFavoriteEntity(
            storyId = story.id,
            name = story.name,
            photoUrl = story.photoUrl,
            description = story.description,
            lat = story.lat,
            lon = story.lon,
            createdAt = story.createdAt
        )
        dao.insert(entity)
    }

    suspend fun removeFavorite(story: Story) {
        dao.delete(
            StoryFavoriteEntity(
                storyId = story.id,
                name = story.name,
                photoUrl = story.photoUrl,
                description = story.description,
                lat = story.lat,
                lon = story.lon,
                createdAt = story.createdAt
            )
        )
    }
}