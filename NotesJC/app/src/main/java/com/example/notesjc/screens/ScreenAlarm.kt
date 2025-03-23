package com.example.notesjc.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.notesjc.MainActivity
import com.example.notesjc.NotificationPlayer
import com.example.notesjc.viewmodels.DBViewModel
import com.example.notesjc.R
import com.example.notesjc.alarm_scheduler.AndroidAlarmScheduler
import com.example.notesjc.data.Note
import com.example.notesjc.service.PlayerService
import kotlinx.serialization.Serializable

@Serializable
data class ScreenAlarm(
    val alarmNoteDateTime: Long?
)

@OptIn(UnstableApi::class)
@Composable
fun ScreenAlarm(
    alarmNoteDateTime: Long?,
    dbViewModel: DBViewModel,
    context: Context,
    scheduler: AndroidAlarmScheduler,
    navController: NavHostController,
    player: NotificationPlayer?
){
    stopPlayerService(context)

    if (alarmNoteDateTime != null)
        dbViewModel.getByDateTime(alarmNoteDateTime)
    val currentNote = dbViewModel.currentNote.collectAsStateWithLifecycle().value

    var  lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver{ _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = colorResource(R.color.white),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f),
                factory = { context ->
                    PlayerView(context).also {
                        it.player = player?.player
                        it.isEnabled = false
                    }
                },
                update = {
                    when (lifecycle) {
                        Lifecycle.Event.ON_STOP -> {
                            it.player?.pause()
                            it.isEnabled = false
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            it.onPause()
                            it.player?.pause()
                            it.isEnabled = false
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            it.onResume()
                            it.player?.play()
                            it.controllerAutoShow = false
                            it.isEnabled = false
                        }

                        else -> {}
                    }
                }
            )
        }

        Button(
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.new_product_blue),
            ),
            onClick = {
                player?.player?.pause()
                scheduler.cancel(currentNote.note)
                dbViewModel.updateCurrentAlarmDateTime(dateTimeMillis = 0L)
                dbViewModel.saveNote(true)
                dbViewModel.setEditAlarmTriggeredNoteState(alarmNoteDateTime ?: 0L)
                navController.navigate(ScreenAdd){
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            }
        ) {
            Text(text = stringResource(R.string.move_to_note))
        }
    }
}

private fun stopPlayerService(context: Context){
    Intent(context, PlayerService::class.java).also {
        it.action = PlayerService.Actions.STOP.toString()
        context.startService(it)
    }
}