package com.example.notesjc.alarm_scheduler

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.notesjc.AlarmReceiver
import com.example.notesjc.data.Note
import kotlinx.serialization.Serializable
import java.time.ZoneId

class AndroidAlarmScheduler(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    @SuppressLint("MissingPermission")
    fun schedule(note: Note) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("Database_item", note)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.alarmDateTime ?: 0,
            PendingIntent.getBroadcast(
                context,
                note.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    fun cancel(note: Note) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                note.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}