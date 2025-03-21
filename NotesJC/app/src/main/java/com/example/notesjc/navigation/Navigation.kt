package com.example.notesjc.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.contains
import androidx.navigation.toRoute
import com.example.mangojc.permissions.PermissionsManager
import com.example.notesjc.NotificationPlayer
import com.example.notesjc.viewmodels.DBViewModel
import com.example.notesjc.R
import com.example.notesjc.alarm_scheduler.AndroidAlarmScheduler
import com.example.notesjc.data.BottomNavItem
import com.example.notesjc.data.FullNote
import com.example.notesjc.screens.ScreenAdd
import com.example.notesjc.screens.ScreenAlarm
import com.example.notesjc.screens.ScreenArchive
import com.example.notesjc.screens.ScreenCategories
import com.example.notesjc.screens.ScreenChooseImages
import com.example.notesjc.screens.ScreenChooseImagesFullScreen
import com.example.notesjc.screens.ScreenNotes
import com.example.notesjc.screens.ScreenSelectedImagesFullScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController,
    dbViewModel: DBViewModel,
    permissionsManager: PermissionsManager,
    context: Context,
    scheduler: AndroidAlarmScheduler,
    player: NotificationPlayer?,
    noteDateTimeID: Long
){
    NavHost(
        navController = navController,
        startDestination = if (noteDateTimeID == 0L) ScreenCategories
        else ScreenAlarm(noteDateTimeID)
    ){
        composable<ScreenCategories>{
            ScreenCategories(dbViewModel, navController)
        }

        composable<ScreenNotes>{
            val args = it.toRoute<ScreenNotes>()
            ScreenNotes(
                dbViewModel,
                navController,
                args.categoryName,
            )
        }

        composable<ScreenAdd>{
            val args = it.toRoute<ScreenAdd>()
            ScreenAdd(
                dbViewModel,
                navController,
                permissionsManager,
                context,
                scheduler,
                args.currentNoteDateTime,
            )
        }

        composable<ScreenArchive>{
            ScreenArchive()
        }

        composable<ScreenChooseImages>{
            ScreenChooseImages(dbViewModel, navController)
        }

        composable<ScreenChooseImagesFullScreen>{
            val args = it.toRoute<ScreenChooseImagesFullScreen>()
            ScreenChooseImagesFullScreen(
                args.currentPosition,
                dbViewModel,
                navController
            )
        }

        composable<ScreenSelectedImagesFullScreen>{
            val args = it.toRoute<ScreenSelectedImagesFullScreen>()
            ScreenSelectedImagesFullScreen(
                args.currentPosition,
                dbViewModel,
                navController
            )
        }

        composable<ScreenAlarm> {
            val args = it.toRoute<ScreenAlarm>()
            ScreenAlarm(
                args.alarmNoteDateTime ?: 0,
                dbViewModel,
                context,
                scheduler,
                navController,
                player
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BottomNavMenu(
    navController: NavHostController,
    dbViewModel: DBViewModel,
    permissionsManager: PermissionsManager,
    context: Context,
    scheduler: AndroidAlarmScheduler,
    player: NotificationPlayer?,
    noteDateTimeID: Long
){
    val bottomNavItems = listOf(
        BottomNavItem(
            route = "com.example.notesjc.screens.ScreenCategories",
            title = stringResource(R.string.categories),
            icon = R.drawable.icon_categories
        ),

        BottomNavItem(
            route = "com.example.notesjc.screens.ScreenAdd",
            title = stringResource(R.string.add),
            icon = R.drawable.icon_add
        ),

        BottomNavItem(
            route = "com.example.notesjc.screens.ScreenArchive",
            title = stringResource(R.string.archive),
            icon = R.drawable.icon_archive
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Log.d("ROUTE", currentDestination?.route.toString())

    Surface {
        Scaffold(
            bottomBar = {
                when(currentDestination?.route){
                    bottomNavItems[0].route,
                    bottomNavItems[2].route,
                    "com.example.notesjc.screens.ScreenNotes/{categoryName}" ->
                        NavigationBar(
                            containerColor = colorResource(R.color.border_blue),
                        ) {
                            bottomNavItems.forEachIndexed { index, bottomNavItem ->
                                NavigationBarItem(
                                    colors = NavigationBarItemColors(
                                        selectedIconColor = colorResource(R.color.new_product_blue),
                                        selectedTextColor = colorResource(R.color.new_product_blue),
                                        selectedIndicatorColor = colorResource(R.color.white),
                                        unselectedIconColor = colorResource(R.color.text_grey_composable),
                                        unselectedTextColor = colorResource(R.color.text_grey_composable),
                                        disabledIconColor = colorResource(R.color.extra_light_grey),
                                        disabledTextColor = colorResource(R.color.extra_light_grey)
                                    ),
                                    selected = currentDestination.hierarchy.any {
                                        it.route == bottomNavItem.route
                                    },
                                    onClick = {
                                        when(index){
                                            0 -> navController.navigate(ScreenCategories){
                                                popUpTo(navController.graph.findStartDestination().id)
                                                launchSingleTop = true
                                            }
                                            1 -> navController.navigate(ScreenAdd(null))
                                            2 -> navController.navigate(ScreenArchive)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = bottomNavItem.icon),
                                            contentDescription = bottomNavItem.title,
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = bottomNavItem.title
                                        )
                                    }
                                )
                            }
                        }
                }

            }
        ) {
            Box(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
                Navigation(
                    navController,
                    dbViewModel,
                    permissionsManager,
                    context,
                    scheduler,
                    player,
                    noteDateTimeID
                )
            }
        }
    }
}