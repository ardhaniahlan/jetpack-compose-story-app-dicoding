package org.apps.composestoryapp.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apps.composestoryapp.MainActivity
import org.apps.composestoryapp.model.Story
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryNotification @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object{
        const val CHANNEL_ID = "stories_channel"
        const val CHANNEL_NAME = "Story Notification"
        const val STORY_ID_KEY = "story_id"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showStoryNotification(story: Story){
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(STORY_ID_KEY, story.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            story.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${story.name} mengupload story baru")
            .setContentText(story.description.take(50))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(story.description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.notify(story.id.hashCode(), notification)
    }
}