package com.anhq.smartalarm.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isActive: Boolean = true,
    val isVibrate: Boolean = true,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val label: String = "",
    val gameType: AlarmGameType = AlarmGameType.NONE,
    val soundUri: String = ""
) {
    // Chuyển repeatDays thành chuỗi hiển thị
    fun getRepeatDaysString(): String {
        if (selectedDays.isEmpty()) return "Once Time"
        
        val orderedDays = selectedDays.sortedBy { 
            when (it) {
                DayOfWeek.MON -> 1
                DayOfWeek.TUE -> 2
                DayOfWeek.WED -> 3
                DayOfWeek.THU -> 4
                DayOfWeek.FRI -> 5
                DayOfWeek.SAT -> 6
                DayOfWeek.SUN -> 7
            }
        }
        return orderedDays.joinToString(", ") { it.label }
    }

    // Tính thời gian đếm ngược từ thời gian hiện tại đến thời gian báo thức
    fun getCountdownTime(currentTimeMillis: Long): String {
        if (!isActive) return "Disabled"

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Nếu không có ngày lặp lại
        if (selectedDays.isEmpty()) {
            var timeInMillis = calendar.timeInMillis
            if (timeInMillis <= currentTimeMillis) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                timeInMillis = calendar.timeInMillis
            }
            return formatCountdown(timeInMillis - currentTimeMillis)
        }

        // Tìm ngày gần nhất trong các ngày được chọn
        var nearestDay = calendar.timeInMillis
        var foundValidDay = false

        selectedDays.forEach { day ->
            val nextTime = calculateNextDayTime(currentTimeMillis, day)
            if (!foundValidDay || nextTime < nearestDay) {
                nearestDay = nextTime
                foundValidDay = true
            }
        }

        return if (foundValidDay) {
            formatCountdown(nearestDay - currentTimeMillis)
        } else {
            "Invalid time"
        }
    }

    private fun calculateNextDayTime(currentTimeMillis: Long, dayOfWeek: DayOfWeek): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetDayOfWeek = when (dayOfWeek) {
            DayOfWeek.MON -> Calendar.MONDAY
            DayOfWeek.TUE -> Calendar.TUESDAY
            DayOfWeek.WED -> Calendar.WEDNESDAY
            DayOfWeek.THU -> Calendar.THURSDAY
            DayOfWeek.FRI -> Calendar.FRIDAY
            DayOfWeek.SAT -> Calendar.SATURDAY
            DayOfWeek.SUN -> Calendar.SUNDAY
        }

        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        var daysToAdd = (targetDayOfWeek - currentDay + 7) % 7

        // Nếu là ngày hiện tại và thời gian đã qua
        if (daysToAdd == 0 && calendar.timeInMillis <= currentTimeMillis) {
            daysToAdd = 7
        }

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        return calendar.timeInMillis
    }

    private fun formatCountdown(timeDifferenceMillis: Long): String {
        if (timeDifferenceMillis <= 0) return "Now"

        val hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis) % 60

        return when {
            hours > 24 -> "${hours / 24}n ${hours % 24}g ${minutes}p"
            hours > 0 -> "${hours}g ${minutes}p"
            hours == 0L && minutes == 0L -> "Sắp tới"
            else -> "${minutes}p"
        }
    }
}