package org.apps.composestoryapp.helper

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("story_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val LAST_NOTIFIED_STORY_ID = "last_notified_story_id"
    }

    fun getLastNotifiedStoryId(): String? {
        return prefs.getString(LAST_NOTIFIED_STORY_ID, null)
    }

    fun setLastNotifiedStoryId(storyId: String) {
        prefs.edit().putString(LAST_NOTIFIED_STORY_ID, storyId).apply()
    }
}