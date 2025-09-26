package net.alienminds.ethnogram.service.utils

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.BuildConfig
import net.alienminds.ethnogram.service.R

class FCMService : FirebaseMessagingService() {

//    init {
//        getNotificationManager(this).createChannel()
//    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        runCatching {
            remoteMessage.notification?.let { rNotify ->
                val notificationManager = getNotificationManager(this)
                notificationManager.createChannel()

                NotificationCompat.Builder(this, CHANNEL_NEWS)
                    .setContentTitle(rNotify.title)
                    .setContentText(rNotify.body)
                    .setSmallIcon(R.mipmap.ic_notify)
                    .build().let { notification ->
                        notificationManager.notify(
                            /* id = */ remoteMessage.messageId.hashCode(),
                            /* notification = */ notification
                        )
                    }
                    }
        }
        Log.d(LOG_TAG, "New FCM message: ${remoteMessage.rawData?.let(::String)}")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(LOG_TAG, "Обновлён FCM токен: $token")
    }

    companion object {
        private const val LOG_TAG = "FCMService"
        private const val TOPIC_ALL = "all"
        private const val CHANNEL_NEWS = "fcm_default_channel"

        suspend fun subscribeNotifications() = runCatching {
//            showDeviceToken()
            FirebaseMessaging
                .getInstance()
                .subscribeToTopic(TOPIC_ALL)
                .await()
        }.onFailure {
            Log.e(LOG_TAG, "Failed to subscribe to notifications", it)
        }.onSuccess {
            Log.d(LOG_TAG, "Subscribed to notifications")
        }

//        suspend fun unsubscribeNotifications() = runCatching{
//            FirebaseMessaging
//                .getInstance()
//                .unsubscribeFromTopic(TOPIC_ALL)
//                .await()
//        }.onFailure {
//            Log.e(LOG_TAG, "Failed to unsubscribe from notifications", it)
//        }.onSuccess {
//            Log.d(LOG_TAG, "Unsubscribed from notifications")
//        }

        private fun NotificationManager.createChannel(){
            runCatching {
                val channelId = CHANNEL_NEWS
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Новости",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                createNotificationChannel(channel)
            }
        }

        private fun getNotificationManager(
            context: Context
        ) = context.getSystemService(
            /*name =*/ NOTIFICATION_SERVICE
        ) as NotificationManager
    }
}

