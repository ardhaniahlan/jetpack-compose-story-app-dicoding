package org.apps.composestoryapp.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StoryFavoriteEntity::class],
    version = 2
)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun storyFavoriteDao(): StoryFavoriteDao
}