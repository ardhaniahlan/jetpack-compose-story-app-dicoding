package org.apps.composestoryapp.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.apps.composestoryapp.model.Story
import org.apps.composestoryapp.model.StoryUi

@Entity(tableName = "story_favorite")
data class StoryFavoriteEntity(
    @PrimaryKey val storyId: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val lat: Float,
    val lon: Float,
    val createdAt: String,
    val likedAt: Long = System.currentTimeMillis()
)

fun StoryFavoriteEntity.toStory(): Story {
    return Story(
        id = storyId,
        name = name,
        description = description,
        photoUrl = photoUrl,
        lat = lat,
        lon = lon,
        createdAt = createdAt
    )
}

fun StoryFavoriteEntity.toStoryUi(): StoryUi {
    return StoryUi(
        story = this.toStory(),
        locationName = null
    )
}