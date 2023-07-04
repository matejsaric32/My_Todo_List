package matejsaric32.android.mytodolist.alarm

import java.time.LocalDateTime

data class AlarmItem(
    val time: LocalDateTime,
    val message: String
)
