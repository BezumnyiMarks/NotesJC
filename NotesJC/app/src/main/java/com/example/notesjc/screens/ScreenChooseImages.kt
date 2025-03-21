package com.example.notesjc.screens

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.notesjc.viewmodels.DBViewModel
import com.example.notesjc.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object ScreenChooseImages

@Composable
fun ScreenChooseImages(
    dbViewModel: DBViewModel,
    navController: NavHostController
){
    val allImagesUri = dbViewModel.mediaStoreImagesUri.collectAsStateWithLifecycle().value
    val chosenImagesUri = dbViewModel.chosenImagesUri.collectAsStateWithLifecycle().value

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .background(color = colorResource(R.color.white))
            .padding(top = 16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icon_accept),
            contentDescription = "",
            tint = colorResource(R.color.new_product_blue),
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable(
                    onClick = {
                        dbViewModel.addCurrentImages(chosenImagesUri)
                        dbViewModel.clearChosenImagesList()
                        navController.popBackStack()
                    }
                )
        )
        LazyVerticalGrid(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp),
            columns = GridCells.Fixed(3),
            content = {
                itemsIndexed(allImagesUri) { index, imageUri ->
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    ){
                        SetImage(
                            image = if (imageUri.toString() != "") imageUri else null,
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                       navController.navigate(ScreenChooseImagesFullScreen(index))
                                    }
                                )
                                .size(150.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentScale = ContentScale.Crop
                        )
                        ChoiceIndicator(
                            selected = chosenImagesUri.contains(imageUri)
                        ){ chosen ->
                            if (chosen)
                                dbViewModel.addChosenImage(imageUri)
                            else dbViewModel.removeChosenImage(imageUri)
                        }
                    }
                }
            }
        )
    }
    BackHandler {
        dbViewModel.clearChosenImagesList()
        navController.popBackStack()
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SetImage(
    image: Any?,
    painter: Painter = painterResource(id = R.drawable.app_poster),
    modifier: Modifier,
    shape: RoundedCornerShape,
    contentScale: ContentScale
){
    Card(
        shape = shape
    ) {
        if (image == null)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = modifier,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )
        else
            GlideImage(
                model = image,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale
            )
    }
}


@Composable
fun ChoiceIndicator(
    selected: Boolean,
    onChooseClick: (Boolean) -> Unit,
){
    var chosen by remember {
        mutableStateOf(selected)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable (
                onClick = {
                    chosen = !chosen
                    onChooseClick(chosen)
                }
            )
            .padding(top = 4.dp, end = 4.dp)
            .background(
                color = colorResource(R.color.white),
                shape = CircleShape
            )
            .alpha(if (chosen) 1f else 0.7f)
            .size(if (!chosen) 18.dp else 24.dp)
            .border(
                width = (if (chosen) 2.dp else 1.dp),
                color = colorResource(id = R.color.new_product_blue),
                shape = CircleShape
            )
    ) {
        if (chosen)
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.icon_done),
                contentDescription = "",
                tint = colorResource(R.color.hint_green),
            )
    }
}

fun getImages(contentResolver: ContentResolver, dbViewModel: DBViewModel){
    val projection = arrayOf(
        MediaStore.Images.Media._ID
    )
    val selection = "${MediaStore.Images.Media._ID}"

    CoroutineScope(Dispatchers.IO).launch {
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val imagesUri = mutableListOf<Uri>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                imagesUri.add(uri)
            }
            dbViewModel.mediaStoreImagesUri.value = imagesUri.reversed()
        }
        this.cancel()
    }
}