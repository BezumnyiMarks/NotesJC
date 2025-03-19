package com.example.notesjc.screens

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
object ScreenArchive

@Composable
fun ScreenArchive(){
    Surface {
        Text(
            text = "ScreenArchive"
        )
    }
}