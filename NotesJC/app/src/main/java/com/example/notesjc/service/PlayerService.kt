package com.example.notesjc.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.notesjc.MainActivity
import com.example.notesjc.NotificationPlayer
import com.example.notesjc.R
import com.example.notesjc.data.Note
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService: Service() {
    @Inject
    lateinit var player: NotificationPlayer

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val note = intent?.getSerializableExtra("Database_item") as Note?
        when(intent?.action){
            Actions.START.toString() -> note?.let { start(it) }
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(note: Note){
        startPlayer()
        startForeground(1, startNotification(note))
    }

    private fun startPlayer(){
        player.player.seekTo(0)
        player.player.play()
        CoroutineScope(Dispatchers.Main).launch {
            delay(30_000)
            player.player.pause()
            stopSelf()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startNotification(note: Note): Notification {
        val i = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("Database_item", note)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val builder = NotificationCompat.Builder(this, "work_list")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(note.category)
            .setAutoCancel(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)

        //val notificationManager = NotificationManagerCompat.from(this)
        //notificationManager.notify(1, builder.build())
        return builder.build()
    }

    enum class Actions{
        START, STOP
    }
}

