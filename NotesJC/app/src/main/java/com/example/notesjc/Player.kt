package com.example.notesjc

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C.VOLUME_FLAG_PLAY_SOUND
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPlayer @OptIn(UnstableApi::class)
@Inject constructor(@ApplicationContext context: Context) {
    val player = ExoPlayer.Builder(context).build()
    init {
        val fileUri = RawResourceDataSource.buildRawResourceUri(R.raw.go_meow)
        val mediaItem = MediaItem.fromUri(fileUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.setDeviceVolume(1, VOLUME_FLAG_PLAY_SOUND)
    }
}