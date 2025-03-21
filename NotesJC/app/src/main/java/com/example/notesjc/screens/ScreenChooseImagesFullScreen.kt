package com.example.notesjc.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.notesjc.R
import com.example.notesjc.viewmodels.DBViewModel
import kotlinx.serialization.Serializable

@Serializable
data class ScreenChooseImagesFullScreen(val currentPosition: Int)

@Composable
fun ScreenChooseImagesFullScreen(
    currentPosition: Int,
    dbViewModel: DBViewModel,
    navController: NavController
){
    val images = dbViewModel.mediaStoreImagesUri.collectAsStateWithLifecycle().value
    val chosenImagesUri = dbViewModel.chosenImagesUri.collectAsStateWithLifecycle().value

    val pagerState = rememberPagerState(pageCount = {images.size})
    var currentPositionFlow by rememberSaveable {
        mutableIntStateOf(currentPosition)
    }
    LaunchedEffect(Unit) {
        pagerState.scrollToPage(currentPosition)
    }
    HorizontalPager(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.text_black_composable)),
        state = pagerState,
        key = { images[it] }
    ) { index ->
        currentPositionFlow = index
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.TopEnd,
            ) {
                SetImage(
                    image = images[index],
                    modifier = Modifier,
                    shape = RoundedCornerShape(0.dp),
                    contentScale = ContentScale.Fit
                )
                ChoiceIndicator(
                    selected = chosenImagesUri.contains(images[index])
                ){ chosen ->
                    if (chosen)
                        dbViewModel.addChosenImage(images[index])
                    else dbViewModel.removeChosenImage(images[index])
                }
            }
        }
    }
}