package com.example.notesjc

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.notesjc.data.Note
import com.example.notesjc.service.PlayerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale.Category
import javax.inject.Inject

class AlarmReceiver: BroadcastReceiver () {
    override fun onReceive(context: Context?, intent: Intent?) {
        val note = intent?.getSerializableExtra("Database_item") as Note
        Log.d("EXTRA_MESSAGE", note.toString())

        Intent(context, PlayerService::class.java).also {
            it.action = PlayerService.Actions.START.toString()
            it.putExtra("Database_item", note)
            context?.startService(it)
        }
    }
}