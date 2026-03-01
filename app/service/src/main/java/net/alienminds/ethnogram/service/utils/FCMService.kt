package net.alienminds.ethnogram.service.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.R
import java.net.URL
import kotlin.jvm.java
import androidx.core.net.toUri

class FCMService : FirebaseMessagingService() {

//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//        runCatching {
//            remoteMessage.notification?.let { rNotify ->
//                val notificationManager = getNotificationManager(this)
//                notificationManager.createChannel()
//
//                NotificationCompat.Builder(this, CHANNEL_NEWS)
//                    .setContentTitle(rNotify.title)
//                    .setContentText(rNotify.body)
//                    .setSmallIcon(R.mipmap.ic_notify)
//                    .build().let { notification ->
//                        notificationManager.notify(
//                            /* id = */ remoteMessage.messageId.hashCode(),
//                            /* notification = */ notification
//                        )
//                    }
//            }
//        }
//        Log.d(LOG_TAG, "New FCM message: ${remoteMessage.rawData?.let(::String)}")
//    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        runCatching {
            Log.d(
                LOG_TAG,
                "New FCM message: ${remoteMessage.rawData?.let(::String) ?: remoteMessage.notification?.toHumanString() ?: remoteMessage.data}"
            )

            val chatId = remoteMessage.data["chatId"]

            if (chatId != null && chatId == ActiveChatTracker.currentChatId) {
                Log.d(LOG_TAG, "User already in this chat — skip notification")
                return
            }
            val notificationManager = getNotificationManager(this)
            val channelId = remoteMessage.notification?.channelId
            notificationManager.createChannel(channelId)
            showNotification(this, remoteMessage, channelId)
        }.onFailure {
            Log.e(LOG_TAG, "Failed to process FCM message", it)
        }
    }

    private fun showNotification(
        context: Context,
        remoteMessage: RemoteMessage,
        channelId: String?
    ) {
        val notification = remoteMessage.notification

        val title = notification?.title
            ?: remoteMessage.data["title"]
            ?: return

        val body = notification?.body
            ?: remoteMessage.data["body"]
            ?: ""

        val manager = getNotificationManager(context)
        val chatId = remoteMessage.data["chatId"]
        val builder = NotificationCompat.Builder(context, channelId?: "fcm_default")
            .setSmallIcon(R.mipmap.ic_notify)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(when(remoteMessage.priority){
                RemoteMessage.PRIORITY_HIGH -> NotificationCompat.PRIORITY_HIGH
                else -> NotificationCompat.PRIORITY_DEFAULT
            })
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .run {
                (chatId?.let {
                    "ethnogram://chat?chatId=$it".toUri()
                }?: remoteMessage.notification?.link)?.let {
                    val pendingIntent = buildLinkPendingIntent(
                        chatId?.hashCode() ?: System.currentTimeMillis().toInt(),
                        it
                    )
                    if (chatId != null){
                        setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    }
                    setContentIntent(pendingIntent)
                }?: this
            }

        // ---- Image support ----
        notification?.imageUrl?.let { imageUrl ->
            try {
                val bitmap = URL(imageUrl.toString())
                    .openStream()
                    .use { BitmapFactory.decodeStream(it) }
                val largeIcon: Bitmap? = null
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(largeIcon)
                )
            } catch (_: Exception) {
                Log.e(LOG_TAG, "Failed to load image: $imageUrl")
            }
        }

        manager.notify(
            chatId?.hashCode() ?: System.currentTimeMillis().toInt(),
            builder.build()
        )
    }

    private fun buildLinkPendingIntent(
        requestCode: Int,
        link: Uri,
    ): PendingIntent? {
        val intent = Intent(
            Intent.ACTION_VIEW,
            link
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun RemoteMessage.Notification.toHumanString() = mapOf(
        "title" to title,
        "body" to body,
        "icon" to icon,
        "imageUrl" to imageUrl,
        "sound" to sound,
        "tag" to tag,
        "color" to color,
        "clickAction" to clickAction,
        "channelId" to channelId,
        "link" to link,
        "ticker" to ticker,
        "notificationPriority" to notificationPriority,
        "visibility" to visibility,
        "notificationCount" to notificationCount,
        "sticky" to sticky,
        "localOnly" to localOnly,
        "defaultSound" to defaultSound,
    )


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(LOG_TAG, "Обновлён FCM токен: $token")
    }

    companion object {
        private const val LOG_TAG = "FCMService"
        private const val TOPIC_ALL = "all"

        suspend fun subscribeUserTopic(userId: String) = runCatching {
            FirebaseMessaging
                .getInstance()
                .subscribeToTopic(userId)
                .await()
        }.onSuccess {
            Log.d(LOG_TAG, "Subscribed to user topic: $userId")
        }.onFailure {
            Log.e(LOG_TAG, "Failed to subscribe to user topic: $userId", it)
        }

        suspend fun unsubscribeUserTopic(userId: String) = runCatching {
            FirebaseMessaging
                .getInstance()
                .unsubscribeFromTopic(userId)
                .await()
        }.onSuccess {
            Log.d(LOG_TAG, "Unsubscribed from user topic: $userId")
        }.onFailure {
            Log.e(LOG_TAG, "Failed to unsubscribe from user topic: $userId", it)
        }

        suspend fun subscribeNotifications() = runCatching {
            FirebaseMessaging
                .getInstance()
                .subscribeToTopic(TOPIC_ALL)
                .await()
        }.onFailure {
            Log.e(LOG_TAG, "Failed to subscribe to notifications", it)
        }.onSuccess {
            Log.d(LOG_TAG, "Subscribed to notifications")
        }

        private fun NotificationManager.createChannel(
            channelId: String?
        ) = runCatching{
            val channelName = mapOf(
                "fcm_default" to "Notifications",
                "fcm_default_channel" to "Notifications",
                "chat_messages" to "Messages"
            )
            val channel = NotificationChannel(
                channelId?: "fcm_default",
                channelName[channelId]?: "Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            createNotificationChannel(channel)
        }.onFailure {
            Log.e(LOG_TAG, "Failed to create notification channel", it)
        }

        private fun getNotificationManager(
            context: Context
        ) = context.getSystemService(
            /*name =*/ NOTIFICATION_SERVICE
        ) as NotificationManager
    }
}

