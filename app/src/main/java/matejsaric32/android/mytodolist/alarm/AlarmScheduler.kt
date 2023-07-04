package matejsaric32.android.mytodolist.alarm

interface AlarmScheduler {

    fun scheduleAlarm(alarmItem: AlarmItem)
    fun cancelAlarm(alarmItem: AlarmItem)
}