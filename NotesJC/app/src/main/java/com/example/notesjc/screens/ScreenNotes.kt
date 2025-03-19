package com.example.notesjc.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.notesjc.viewmodels.DBViewModel
import com.example.notesjc.R
import com.example.notesjc.data.FullNote
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ScreenNotes(val categoryName: String)

@Composable
fun ScreenNotes(
    dbViewModel: DBViewModel,
    navController: NavController,
    categoryName: String
){
    dbViewModel.getByCategory(categoryName)
    val categoryNotes = dbViewModel.allNotes.collectAsState().value

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

    if (lifecycle == Lifecycle.Event.ON_RESUME)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                text = categoryName,
                color = colorResource(R.color.text_black_composable),
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )

            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                state = rememberLazyListState(),
            ) {
                item {
                    categoryNotes.forEachIndexed{ index, note ->
                        NoteView(
                            note,
                        ){
                            navController.navigate(ScreenAdd(note.note?.noteDateTimeID))
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = if (index != categoryNotes.lastIndex)
                                colorResource(R.color.divider)
                            else Color.Transparent,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    SetImage(
                        null,
                        painterResource(id = R.drawable.app_poster2),
                        Modifier,
                        RoundedCornerShape(0),
                        ContentScale.Crop
                    )
                }
            }
        }
}

@Composable
fun NoteView(
    note: FullNote,
    onNoteClicked: () -> Unit
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            onClick = {
                onNoteClicked()
            }
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        color = when(note.note?.priority){
                            1 -> colorResource(R.color.promo_red)
                            2 -> colorResource(R.color.yellow)
                            else -> colorResource(R.color.hint_green)
                        },
                        shape = CircleShape
                    )
                    .size(24.dp)
            ) {

            }

            if (note.note?.alarmDateTime != null && note.note?.alarmDateTime != 0L)
                Icon(
                    painter = painterResource(R.drawable.icon_add_alert),
                    contentDescription = null,
                    tint = colorResource(R.color.yellow)
                )

            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                text = note.note?.description ?: "",
                fontSize = 24.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(R.color.text_black_composable)
            )

            Icon(
                modifier = Modifier.padding(start = 16.dp),
                painter = painterResource(R.drawable.icon_arrow),
                contentDescription = null,
                tint = colorResource(R.color.new_product_blue)
            )
        }
    }
}