package com.example.notesjc.common_views

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.notesjc.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimePicker(
    dbTimeInMillis: Long?,
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
){
    var datePickerOpen by rememberSaveable {
        mutableStateOf(true)
    }

    var timePickerOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var initialCalendar by rememberSaveable {
        mutableStateOf(Calendar.getInstance())
    }

    if(datePickerOpen)
        DatePickerDialog(
            dbTimeInMillis = if (dbTimeInMillis == 0L) null else dbTimeInMillis,
            onDateSelected = { selectedDate ->
                initialCalendar = selectedDate
                timePickerOpen = true
                datePickerOpen = false
            },
            onDismiss = {
                onDismiss()
                datePickerOpen = false
            }
        )

    if (timePickerOpen)
        TimePickerDialog(
            dbTimeInMillis = if (dbTimeInMillis == 0L) null else dbTimeInMillis,
            initialCalendar = initialCalendar ,
            onTimeSelected = { timeInMillis ->
                onDateTimeSelected(timeInMillis)
                timePickerOpen = false
            },
            onDismiss = {
                onDismiss()
                timePickerOpen = false
            }
        )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    dbTimeInMillis: Long?,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis >= System.currentTimeMillis() - 86_400_000
        }
    })

    if (dbTimeInMillis != null)
        datePickerState.selectedDateMillis

    val selectedDateMillis = datePickerState.selectedDateMillis

    DatePickerDialog(
        colors = DatePickerDefaults.colors(
            containerColor = colorResource(R.color.white),
            todayDateBorderColor = colorResource(R.color.new_product_blue),
            selectedYearContainerColor = colorResource(R.color.new_product_blue),
            selectedDayContainerColor = colorResource(R.color.new_product_blue),
        ),
        onDismissRequest = {onDismiss()},
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.new_product_blue),
                ),
                onClick = {
                    if (selectedDateMillis != null){
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = selectedDateMillis
                        onDateSelected(calendar)
                    }
                    else onDismiss()
                }

            ) {
                Text(text = "Ок")
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.new_product_blue),
                ),
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            colors = DatePickerDefaults.colors(
                containerColor = colorResource(R.color.white),
                todayDateBorderColor = colorResource(R.color.new_product_blue),
                todayContentColor = colorResource(R.color.new_product_blue),
                selectedYearContainerColor = colorResource(R.color.new_product_blue),
                selectedDayContainerColor = colorResource(R.color.new_product_blue),
                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.new_product_blue),
                    focusedLabelColor = colorResource(R.color.new_product_blue),
                    unfocusedBorderColor = colorResource(R.color.text_grey_composable),
                    cursorColor = colorResource(R.color.text_black_composable),
                )
            ),
            state = datePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    dbTimeInMillis: Long?,
    initialCalendar: Calendar,
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
){
    val timePickerState = rememberTimePickerState(
        is24Hour = false,
        initialHour = getHour(dbTimeInMillis ?: Calendar.getInstance().timeInMillis),
        initialMinute = getMinute(dbTimeInMillis ?: Calendar.getInstance().timeInMillis)
    )

    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = Modifier
                .background(
                    color = colorResource(R.color.white),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = colorResource(R.color.white),
                        clockDialColor = colorResource(R.color.extra_light_blue),
                        clockDialSelectedContentColor = colorResource(R.color.white),
                        clockDialUnselectedContentColor = colorResource(R.color.text_black_composable),
                        selectorColor = colorResource(R.color.new_product_blue),
                        periodSelectorBorderColor = colorResource(R.color.new_product_blue),
                        periodSelectorSelectedContainerColor = colorResource(R.color.new_product_blue),
                        periodSelectorUnselectedContainerColor = colorResource(R.color.white),
                        periodSelectorSelectedContentColor = colorResource(R.color.white),
                        periodSelectorUnselectedContentColor = colorResource(R.color.text_black_composable),
                        timeSelectorSelectedContainerColor = colorResource(R.color.new_product_blue),
                        timeSelectorUnselectedContainerColor = colorResource(R.color.extra_light_blue),
                        timeSelectorSelectedContentColor = colorResource(R.color.white),
                        timeSelectorUnselectedContentColor = colorResource(R.color.text_black_composable),
                    )
                )

                Row(
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.new_product_blue),
                        ),
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.new_product_blue),
                        ),
                        onClick = {
                            initialCalendar.set(
                                initialCalendar.get(Calendar.YEAR),
                                initialCalendar.get(Calendar.MONTH),
                                initialCalendar.get(Calendar.DATE),
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onTimeSelected(initialCalendar.timeInMillis)
                        }

                    ) {
                        Text(text = "Ок")
                    }
                }
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
 fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    return formatter.format(Date(millis))
}

@RequiresApi(Build.VERSION_CODES.O)
private fun convertDateToMillis(date: String): Long {
    val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE).atStartOfDay()
    val calendar = Calendar.getInstance()
    calendar.set(localDate.year, localDate.monthValue, localDate.dayOfMonth)
    return calendar.timeInMillis
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getDate(date: String): String{
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = convertDateToMillis(date)
    var day = calendar.get(Calendar.DAY_OF_MONTH).toString()
    if (day.length == 1)
        day = "0$day"
    var month = calendar.get(Calendar.MONTH).toString()
    if (month.length == 1)
        month = "0$month"
    return "$day.$month.${calendar.get(Calendar.YEAR)}"
}

fun getHour(timeInMillis: Long): Int{
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis
    return calendar.get(Calendar.HOUR_OF_DAY)
}

fun getMinute(timeInMillis: Long): Int{
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis
    return calendar.get(Calendar.MINUTE)
}