package com.example.notesjc

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mangojc.permissions.PermissionsManager
import com.example.notesjc.alarm_scheduler.AndroidAlarmScheduler
import com.example.notesjc.data.Note
import com.example.notesjc.navigation.BottomNavMenu
import com.example.notesjc.navigation.Navigation
import com.example.notesjc.screens.ScreenAlarm
import com.example.notesjc.viewmodels.DBViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val dbViewModel: DBViewModel by viewModels()
    @Inject
    lateinit var player: NotificationPlayer
    private val permissionsManager = PermissionsManager(this)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager.checkPermissions()
        observeIntent()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent{
            //enableEdgeToEdge()
            val scheduler = AndroidAlarmScheduler(this)
            val navController = rememberNavController()
            val noteDateTimeID = dbViewModel.onNewIntentGetExtra.collectAsStateWithLifecycle().value

            Column(
                modifier = Modifier.statusBarsPadding()
            ) {
                BottomNavMenu(
                    navController,
                    dbViewModel,
                    permissionsManager,
                    this@MainActivity,
                    scheduler,
                    player,
                    noteDateTimeID
                )
            }
        }
    }

    private fun observeIntent() {
        val item = intent?.getSerializableExtra("Database_item") as Note?
        dbViewModel.onNewIntentGetExtra.value = item?.noteDateTimeID ?: 0L
    }
}

