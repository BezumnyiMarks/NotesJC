package com.example.notesjc.common_views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesjc.viewmodels.DBViewModel
import com.example.notesjc.R
import com.example.notesjc.data.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityChangeBottomSheet(
    dbViewModel: DBViewModel,
    onDismiss: (Int) -> Unit
){
    val currentNote = dbViewModel.currentNote.collectAsState().value
    val bottomSheetState = rememberModalBottomSheetState()

    if (currentNote.note?.priority == null)
        currentNote.note = currentNote.note?.copy(priority = 3)

    val radioButtons = remember {
        mutableStateListOf(
            Priority(
                priority = 1,
                selected = currentNote.note?.priority == 1
            ),
            Priority(
                priority = 2,
                selected = currentNote.note?.priority == 2
            ),
            Priority(
                priority = 3,
                selected = currentNote.note?.priority == 3
            )
        )
    }

    var selectedPriority by rememberSaveable {
        mutableStateOf(currentNote.note?.priority)
    }

    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            selectedPriority = radioButtons.find {
                it.selected
            }?.priority
            onDismiss(selectedPriority ?: 3)
        },
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            radioButtons.forEach { priority ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable (
                            onClick = {
                                radioButtons.replaceAll{
                                    it.copy(selected = it.priority == priority.priority)
                                }
                                selectedPriority = radioButtons.find {
                                    it.selected
                                }?.priority
                                coroutineScope.launch {
                                    delay(200)
                                    onDismiss(selectedPriority ?: 3)
                                }
                            }
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable (
                                onClick = {
                                    radioButtons.replaceAll{
                                        it.copy(selected = it.priority == priority.priority)
                                    }
                                    selectedPriority = radioButtons.find {
                                        it.selected
                                    }?.priority
                                    coroutineScope.launch {
                                        delay(200)
                                        onDismiss(selectedPriority ?: 3)
                                    }
                                }
                            )
                            .background(
                                color = when(priority.priority){
                                    1 -> colorResource(R.color.promo_red)
                                    2 -> colorResource(R.color.yellow)
                                    else -> colorResource(R.color.hint_green)
                                },
                                shape = CircleShape
                            )
                            .alpha(1f)
                            .size(24.dp)
                    ) {

                    }

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.Transparent
                    )

                    Text(
                        textAlign = TextAlign.Center,
                        modifier = Modifier,
                        text = when(priority.priority){
                            1 -> stringResource(R.string.urgently)
                            2 -> stringResource(R.string.middle)
                            else -> stringResource(R.string.relax)
                        },
                        fontSize = 18.sp,
                        color = colorResource(R.color.text_black_composable)
                    )
                    RadioButton(
                        colors = RadioButtonColors(
                            selectedColor = colorResource(R.color.new_product_blue),
                            unselectedColor = colorResource(R.color.text_black_composable),
                            disabledSelectedColor = colorResource(R.color.extra_light_blue),
                            disabledUnselectedColor = colorResource(R.color.extra_light_grey),
                        ),
                        selected = priority.selected,
                        onClick = {
                            radioButtons.replaceAll{
                                it.copy(selected = it.priority == priority.priority)
                            }
                            selectedPriority = radioButtons.find {
                                it.selected
                            }?.priority
                            coroutineScope.launch {
                                delay(200)
                                onDismiss(selectedPriority ?: 3)
                            }
                        }
                    )
                }
            }
        }
    }
}