package com.example.notesjc.screens

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.mangojc.permissions.PermissionsManager
import com.example.notesjc.viewmodels.DBViewModel
import com.example.notesjc.R
import com.example.notesjc.alarm_scheduler.AndroidAlarmScheduler
import com.example.notesjc.common_views.DateTimePicker
import com.example.notesjc.common_views.PriorityChangeBottomSheet
import com.example.notesjc.common_views.keyboardBottomAsState
import com.example.notesjc.common_views.keyboardTopAsState
import com.example.notesjc.data.FullNote
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class ScreenAdd(
    val currentNoteDateTime: Long?,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenAdd(
    dbViewModel: DBViewModel,
    navController: NavHostController,
    permissionsManager: PermissionsManager,
    context: Context,
    scheduler: AndroidAlarmScheduler,
    currentNoteDateTime: Long?,
){
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

    val currentNote = dbViewModel.currentNote.collectAsStateWithLifecycle().value
    if (currentNoteDateTime != null && currentNote == FullNote()){
        LaunchedEffect(Unit) {
            dbViewModel.getByDateTime(currentNoteDateTime)
        }
    }

    Log.d("ADD", currentNote.toString())

    if (lifecycle == Lifecycle.Event.ON_RESUME){
        var category by rememberSaveable {
            mutableStateOf(currentNote.note?.category ?: "")
        }
        var description by rememberSaveable {
            mutableStateOf(currentNote.note?.description ?: "")
        }
        var dateTimePickerOpen by rememberSaveable {
            mutableStateOf(false)
        }
        var bottomSheetOpen by rememberSaveable {
            mutableStateOf(false)
        }
        var saveAlertDialogOpen by rememberSaveable {
            mutableStateOf(false)
        }
        var saveConfirmDialogOpen by rememberSaveable {
            mutableStateOf(false)
        }
        val keyboardBottom by keyboardBottomAsState()
        Log.d("KEY", keyboardBottom.toString())
        Log.d("screen",LocalConfiguration.current.screenHeightDp.dp.toString())

        Column(
            modifier = Modifier
                .imePadding()
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ){
                DataEditView(
                    dbViewModel,
                    category,
                    description,
                    navController,
                    onCategoryChange = { categoryText ->
                        dbViewModel.updateCurrentCategory(categoryText)
                        category = categoryText
                    },
                    onNoteChange = { descriptionText ->
                        dbViewModel.updateCurrentDescription(descriptionText)
                        description = descriptionText
                    }
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ){
                BottomPanelView(
                    dbViewModel = dbViewModel,
                    onRemindClick = {
                        dateTimePickerOpen = true
                    },
                    onImageClick = {
                        if (permissionsManager.checkPermissions())
                            getImages(context.contentResolver, dbViewModel)
                        navController.navigate(ScreenChooseImages)
                    },
                    onPriorityClick = {
                        bottomSheetOpen = true
                    },
                    onSaveClick = {
                        saveConfirmDialogOpen = true
                    }
                )
            }
        }

        if (dateTimePickerOpen)
            DateTimePicker(
                null,
                onDateTimeSelected = { alarmDateTime ->
                    dbViewModel.updateCurrentAlarmDateTime(alarmDateTime)
                    dateTimePickerOpen = false
                },
                onDismiss = {
                    dateTimePickerOpen = false
                }
            )

        if (bottomSheetOpen)
            PriorityChangeBottomSheet(dbViewModel){ selectedPriority ->
                dbViewModel.updateCurrentPriority(selectedPriority)
                bottomSheetOpen = false
            }

        if (saveConfirmDialogOpen)
            SaveConfirmDialog(
                onCancelClick = {
                    saveConfirmDialogOpen = false
                },
                onSaveClick = {
                    dbViewModel.saveNote(true)
                    currentNote.note?.let { note ->
                        if (note.alarmDateTime != null)
                            scheduler.schedule(note)
                    }
                    navController.navigate(ScreenCategories){
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                    dbViewModel.clearIntentExtra()
                    saveConfirmDialogOpen = false
                }
            )

        if (saveAlertDialogOpen)
            SaveAlertDialog(
                onCancelClick = {
                    saveAlertDialogOpen = false
                },
                onExitClick = {
                    navController.navigate(ScreenCategories){
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                    dbViewModel.clearIntentExtra()
                    dbViewModel.clearCurrentNote()
                    saveAlertDialogOpen = false
                }
            )
        BackHandler {
            saveAlertDialogOpen = true
        }
    }
}

@Composable
fun BottomPanelView(
    dbViewModel: DBViewModel,
    onRemindClick: () -> Unit,
    onImageClick: () -> Unit,
    onPriorityClick: () -> Unit,
    onSaveClick: () -> Unit,
){
    val currentNote = dbViewModel.currentNote.collectAsState().value
    Row(
        modifier = Modifier
            .height(100.dp)
            .padding(top = 16.dp),
    ) {
        if (currentNote.note?.alarmDateTime == null || currentNote.note?.alarmDateTime == 0L)
            BottomPanelItem(
                modifier = Modifier
                    .weight(1f),
                painter = painterResource(R.drawable.icon_add_alert),
                description = stringResource(R.string.add_reminder),
                color = colorResource(R.color.new_product_blue)
            ) {
                onRemindClick()
            }
        else
            Row (
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ){
                    BottomPanelItem(
                        modifier = Modifier.fillMaxWidth(),
                        painter = painterResource(R.drawable.icon_add_alert),
                        description = stringResource(R.string.change_time),
                        color = colorResource(R.color.yellow)
                    ) {
                        onRemindClick()
                    }
                    RemoveView(
                        paddingEnd = 9.dp
                    ) {
                        dbViewModel.updateCurrentAlarmDateTime(null)
                    }
                }
            }
        VerticalDivider(
            thickness = 1.dp,
            color = colorResource(R.color.divider)
        )

        BottomPanelItem(
            modifier = Modifier
                .weight(1f),
            painter = painterResource(R.drawable.icon_add_image),
            description = stringResource(R.string.add_image),
            color = colorResource(R.color.new_product_blue)
        ) {
            onImageClick()
        }
        VerticalDivider(
            thickness = 1.dp,
            color = colorResource(R.color.divider)
        )

        BottomPanelItem(
            modifier = Modifier
                .weight(1f),
            painter = painterResource(R.drawable.icon_priority),
            description = stringResource(R.string.change_priority),
            color = when(currentNote.note?.priority){
                1 -> colorResource(R.color.promo_red)
                2 -> colorResource(R.color.yellow)
                else -> colorResource(R.color.hint_green)
            }
        ) {
            onPriorityClick()
        }
        VerticalDivider(
            thickness = 1.dp,
            color = colorResource(R.color.divider)
        )

        BottomPanelItem(
            modifier = Modifier
                .weight(1f),
            painter = painterResource(R.drawable.icon_save),
            description = stringResource(R.string.save),
            colorResource(R.color.new_product_blue)
        ) {
            onSaveClick()
        }
    }
}

@Composable
fun BottomPanelItem(
    modifier: Modifier,
    painter: Painter,
    description:String,
    color: Color,
    onClick: () -> Unit
){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(54.dp)
            .clickable {
                onClick()
            }
    ) {
        Icon(
            modifier = Modifier.weight(1f),
            painter = painter,
            contentDescription = "",
            tint = color,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            maxLines = 3,
            text = description,
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            color = colorResource(R.color.new_product_blue)
        )
    }
}

@Composable
fun DataEditView(
    dbViewModel: DBViewModel,
    category: String,
    note: String,
    navController: NavController,
    onCategoryChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
){
    val currentNote = dbViewModel.currentNote.collectAsStateWithLifecycle().value
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextDataField(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            singleLine = true,
            value = category,
            hint = stringResource(R.string.category_hint)
        ) {
            onCategoryChange(it)
        }

        if (currentNote.images?.isNotEmpty() == true)
            LazyRow(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                itemsIndexed(currentNote.images ?: listOf()){index, image ->
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    ){
                        SetImage(
                            image = if (image.imageURI.toString() != "") image.imageURI else null,
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        navController.navigate(ScreenSelectedImagesFullScreen(index))
                                    }
                                )
                                .size(75.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentScale = ContentScale.Crop
                        )
                        RemoveView(
                            paddingTop = 3.dp
                        ) {
                            dbViewModel.removeCurrentImage(image)
                        }
                    }
                }
            }

        TextDataField(
            modifier = Modifier
                .fillMaxSize(),
            singleLine = false,
            value = note,
            hint = stringResource(R.string.note_hint)
        ) {
            onNoteChange(it)
        }
    }
}

@Composable
fun SaveAlertDialog(
    onCancelClick: () -> Unit,
    onExitClick: () -> Unit
){
    DialogView(
        dialogTitle = stringResource(R.string.alert_title),
        dialogText = stringResource(R.string.exit_confirm_dialog_text),
        cancelBtnText = stringResource(R.string.cancel),
        confirmBtnTExt = stringResource(R.string.exit),
        onCancelClick = {
            onCancelClick()
        },
        onConfirmClick = {
            onExitClick()
        }
    )
}

@Composable
fun SaveConfirmDialog(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
){
    DialogView(
        dialogTitle = stringResource(R.string.save_title),
        dialogText = stringResource(R.string.save_confirm_dialog_text),
        cancelBtnText = stringResource(R.string.cancel),
        confirmBtnTExt = stringResource(R.string.save),
        onCancelClick = {
            onCancelClick()
        },
        onConfirmClick = {
            onSaveClick()
        }
    )
}

@Composable
fun DialogView(
    dialogTitle: String,
    dialogText: String,
    cancelBtnText: String,
    confirmBtnTExt: String,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit
){
    Dialog(
        onDismissRequest = {
            onCancelClick()
        }
    ) {
        Column (
            modifier = Modifier
                .background(
                    color = colorResource(R.color.white),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                textAlign = TextAlign.Center,
                text = dialogTitle.uppercase(),
                color = colorResource(R.color.text_black_composable),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                text = dialogText,
                color = colorResource(R.color.text_black_composable)
            )
            Row (
                horizontalArrangement = Arrangement.Center,
            ){
                Button(
                    modifier = Modifier
                        .padding(end = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.new_product_blue),
                    ),
                    onClick = {
                        onCancelClick()
                    }
                ) {
                    Text(text = cancelBtnText)
                }
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.new_product_blue),
                    ),
                    onClick = {
                        onConfirmClick()
                    }
                ) {
                    Text(confirmBtnTExt)
                }
            }
        }
    }
}

@Composable
fun TextDataField(
    modifier: Modifier,
    singleLine: Boolean,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
){
    OutlinedTextField(
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(R.color.new_product_blue),
            unfocusedBorderColor = colorResource(R.color.text_grey_composable),
            cursorColor = colorResource(R.color.text_black_composable),
        ),
        singleLine = singleLine,
        textStyle = TextStyle(
            color = colorResource(R.color.text_black_composable),
            fontSize = 22.sp
        ),
        modifier = modifier,
        placeholder = {
            Text(
                text = hint,
                color = colorResource(R.color.text_grey_composable)
            )
        },
        value = value,
        onValueChange = {
            onValueChange(it)
        }
    )
}

@Composable
fun RemoveView(
    paddingTop: Dp = 3.dp,
    paddingEnd: Dp = 3.dp,
    onRemoveClick: () -> Unit,
){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(
                onClick = {
                    onRemoveClick()
                }
            )
            .padding(top = 3.dp, end = paddingEnd)
            .alpha(0.7f)
            .size(18.dp)
            .border(
                width = 2.dp,
                color = colorResource(id = R.color.promo_red),
                shape = CircleShape
            )
    ) {
        Icon(
            modifier = Modifier.rotate(45f),
            painter = painterResource(id = R.drawable.icon_add),
            contentDescription = "",
            tint = colorResource(R.color.promo_red)
        )
    }
}

