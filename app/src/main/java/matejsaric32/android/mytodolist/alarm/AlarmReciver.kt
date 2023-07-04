package matejsaric32.android.mytodolist.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReciver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("message") ?: return
        println(message)
    }
}