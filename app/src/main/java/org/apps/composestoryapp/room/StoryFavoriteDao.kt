package org.apps.composestoryapp.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryFavoriteDao {

    @Query("SELECT * FROM story_favorite ORDER BY likedAt DESC")
    fun getFavorites(): Flow<List<StoryFavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM story_favorite WHERE storyId = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: StoryFavoriteEntity)

    @Delete
    suspend fun delete(favorite: StoryFavoriteEntity)
}
