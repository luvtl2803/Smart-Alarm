package com.anhq.smartalarm.core.model

import com.anhq.smartalarm.core.database.model.AlarmEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val repeatDays: List<Int>, // Danh sách các ngày lặp lại (0 = Chủ nhật, 1 = Thứ Hai, ...)
    val label: String,
    val isEnabled: Boolean,
    val isVibrate: Boolean,
    val timeInMillis: Long // Thêm thuộc tính timeInMillis để lưu thời gian báo thức
) {
    // Chuyển repeatDays thành chuỗi hiển thị (ví dụ: "Mon, Tue, Thu, Fri")
    fun getRepeatDaysString(): String {
        if (repeatDays.isEmpty()) return "No repeat"
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return repeatDays.sorted().joinToString(", ") { dayNames[it] }
    }

    // Tính thời gian đếm ngược từ thời gian hiện tại đến thời gian báo thức
    fun getCountdownTime(currentTimeMillis: Long): String {
        if (!isEnabled) return "Disabled"

        val timeDifference = timeInMillis - currentTimeMillis
        if (timeDifference <= 0) {
            // Nếu thời gian báo thức đã qua, có thể cần tính lại thời gian cho lần lặp tiếp theo
            return calculateNextAlarmTime(currentTimeMillis)
        }

        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60
        return "${hours}h ${minutes}m"
    }

    // Tính thời gian báo thức tiếp theo nếu thời gian hiện tại đã qua
    private fun calculateNextAlarmTime(currentTimeMillis: Long): String {
        if (repeatDays.isEmpty()) return "Passed"

        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
        }
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Chủ nhật, 1 = Thứ Hai, ...

        // Tìm ngày lặp lại tiếp theo gần nhất
        var daysToAdd = 1
        while (true) {
            val nextDay = (currentDay + daysToAdd) % 7
            if (repeatDays.contains(nextDay)) break
            daysToAdd++
        }

        // Cập nhật thời gian cho lần báo thức tiếp theo
        calendar.apply {
            add(Calendar.DAY_OF_MONTH, daysToAdd)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nextTimeInMillis = calendar.timeInMillis
        val timeDifference = nextTimeInMillis - currentTimeMillis
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60
        return "${hours}h ${minutes}m"
    }
}

fun AlarmEntity.toAlarm() = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    repeatDays = repeatDays,
    label = label,
    isEnabled = isEnabled,
    isVibrate = isVibrate,
    timeInMillis = timeInMillis
)

fun Alarm.toAlarmEntity() = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    repeatDays = repeatDays,
    label = label,
    isEnabled = isEnabled,
    isVibrate = isVibrate,
    timeInMillis = timeInMillis
)

fun List<AlarmEntity>.mapToAlarms() = map { it.toAlarm() }

fun AlarmEntity.mapToAlarm() = toAlarm()