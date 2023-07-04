package matejsaric32.android.mytodolist.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.activities.IntroActivity
import matejsaric32.android.mytodolist.activities.MainActivity
import matejsaric32.android.mytodolist.activities.SplashScreenActivity
import matejsaric32.android.mytodolist.utils.Constants
import kotlin.random.Random

class FCMservices : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "From: ${message.toString()}")
        Log.d("FCM", "Data: ${message.data.toString()}")

        message.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: " + message.data)
            val title = message.data[Constants.FCM_KEY_TITLE]!!
            val message = message.data[Constants.FCM_KEY_MESSAGE]!!

            sendNotification(title, message)
        }

        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        val sharedPreferences =
            this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }

    private fun sendNotification(title: String,messageBody: String) {
        val intent = if (FirestoreClass().getCurrentUserID().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        }else{
            Intent(this, IntroActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = Random.nextInt()
        val defaultSoundUri =
            android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId.toString())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId.toString(),
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())

    }
}