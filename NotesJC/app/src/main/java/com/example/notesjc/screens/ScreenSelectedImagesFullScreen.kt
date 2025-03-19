package com.example.notesjc.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notesjc.R
import com.example.notesjc.viewmodels.DBViewModel
import kotlinx.serialization.Serializable

@Serializable
data class ScreenSelectedImagesFullScreen(val currentPosition: Int)

@Composable
fun ScreenSelectedImagesFullScreen(
    currentPosition: Int,
    dbViewModel: DBViewModel,
    navController: NavController
){
    val images = dbViewModel.currentNote.collectAsState().value.images
    val pagerState = rememberPagerState(pageCount = {images?.size ?: 0})
    LaunchedEffect(Unit) {
        pagerState.scrollToPage(currentPosition)
    }
    HorizontalPager(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.text_black_composable)),
        state = pagerState,
        key = { images?.get(it)?.imageURI ?: "" }
    ) { index ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SetImage(
                image = images?.get(index)?.imageURI,
                modifier = Modifier,
                shape = RoundedCornerShape(0.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}