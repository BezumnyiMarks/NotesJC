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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.notesjc.viewmodels.DBViewModel
import com.example.notesjc.R
import com.example.notesjc.data.FullNote
import kotlinx.serialization.Serializable

@Serializable
object ScreenCategories

@Composable
fun ScreenCategories(dbViewModel: DBViewModel, navController: NavController){
    dbViewModel.getAll()
    val allNotes = dbViewModel.allNotes.collectAsState().value
    val allCategoriesNames = mutableListOf<String>()
    if (allNotes.isNotEmpty())
        allNotes.forEach{
            if (!allCategoriesNames.contains(it.note?.category?:""))
                allCategoriesNames.add(it.note?.category ?: "")
        }

    var lifecycle by remember {
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

   // Box(
   //     contentAlignment = Alignment.TopCenter,
   //     modifier = Modifier.fillMaxWidth()
   // ){
        //Column(
        //    modifier = Modifier.padding(top = 16.dp)
        //) {
        //}
        if (lifecycle == Lifecycle.Event.ON_RESUME)
            //Card(
            //    colors = CardDefaults.cardColors(
            //        containerColor = colorResource(R.color.white)
            //    ),
            //    shape = RoundedCornerShape(0),
            //    elevation = CardDefaults.cardElevation(1.dp)
            //) {
                LazyColumn (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ){
                    item {
                        allCategoriesNames.forEachIndexed{ index, categoryName ->
                            val categoryNotes = mutableListOf<FullNote>()
                            allNotes.forEach {
                                if (it.note?.category == categoryName)
                                    categoryNotes.add(it)
                            }
                            CategoryView(
                                categoryName,
                                categoryNotes,
                                categoryNotes.size,
                            ){
                                navController.navigate(ScreenNotes(categoryName))
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = if (index != allCategoriesNames.lastIndex)
                                    colorResource(R.color.divider)
                                else Color.Transparent,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

                        SetImage(
                            null,
                            painterResource(id = R.drawable.app_poster),
                            Modifier,
                            RoundedCornerShape(0),
                            ContentScale.Crop
                        )
                    }
                }
            //}
    //}
}

@Composable
fun CategoryView(
    categoryName: String,
    categoryNotes: List<FullNote>,
    notesAmount: Int,
    onCategoryClicked: () -> Unit
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            onClick = {
                onCategoryClicked()
            }
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp)
        ) {
            PriorityColumn(categoryNotes)

            if (categoryNotes.contains(categoryNotes.find { (it.note?.alarmDateTime ?: 0L) > 0L }))
                Icon(
                    painter = painterResource(R.drawable.icon_add_alert),
                    contentDescription = null,
                    tint = colorResource(R.color.yellow)
                )

            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                text = categoryName,
                fontSize = 24.sp,
                color = colorResource(R.color.text_black_composable)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    notesAmount.toString(),
                    fontSize = 14.sp,
                    color = colorResource(R.color.new_product_blue)
                )

                Icon(
                    painter = painterResource(R.drawable.icon_arrow),
                    contentDescription = null,
                    tint = colorResource(R.color.new_product_blue)
                )
            }
        }
    }
}

@Composable
fun PriorityColumn(categoryNotes: List<FullNote>){
    val prioritiesList = listOf(1, 2, 3)
    var prioritiesCounter = 0
    Column (
        modifier = Modifier
            .padding(end = 8.dp)
    ){
        prioritiesList.forEach {priority ->
            if (categoryNotes.contains(categoryNotes.find { it.note?.priority == priority })) {
                prioritiesCounter++
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = if (prioritiesCounter > 1) 8.dp else 0.dp)
                        .background(
                            color = when (priority) {
                                1 -> colorResource(R.color.promo_red)
                                2 -> colorResource(R.color.yellow)
                                else -> colorResource(R.color.hint_green)
                            },
                            shape = CircleShape
                        )
                        .size(24.dp)
                ) {

                }
            }
        }
    }
}