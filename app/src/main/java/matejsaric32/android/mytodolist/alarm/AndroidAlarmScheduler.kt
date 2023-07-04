package matejsaric32.android.mytodolist.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId


class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler{

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun scheduleAlarm(alarmItem: AlarmItem) {

        val intent = Intent(context, AlarmReciver::class.java).apply {
            putExtra("message", alarmItem.message)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            alarmItem.time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            PendingIntent.getBroadcast(context,
                alarmItem.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        )
    }

    override fun cancelAlarm(alarmItem: AlarmItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(context,
                alarmItem.hashCode(),
                Intent(context, AlarmReciver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        )
    }

}