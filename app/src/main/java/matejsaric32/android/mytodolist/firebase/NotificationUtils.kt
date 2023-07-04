package matejsaric32.android.mytodolist.firebase

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationUtils {

    fun sendNotificationToUser(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d("Notification", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("Notification", response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e("Notification", e.toString())
        }

    }

}