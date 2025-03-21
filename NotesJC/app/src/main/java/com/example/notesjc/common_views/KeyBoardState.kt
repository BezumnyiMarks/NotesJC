package com.example.notesjc.common_views

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView

@Composable
fun keyboardTopAsState(): State<Int> {
    val top = WindowInsets.ime.getTop(LocalDensity.current)
    return rememberUpdatedState(top)
}

@Composable
fun keyboardBottomAsState(): State<Int> {
    val bottom = WindowInsets.ime.getBottom(LocalDensity.current)
    return rememberUpdatedState(bottom)
}

enum class Keyboard {
    Opened, Closed
}

@Composable
fun keyboardAsState(): State<Int> {
    val keyboardState = remember { mutableIntStateOf(0) }
    val view = LocalView.current
    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.intValue = if (keypadHeight > screenHeight * 0.15) keypadHeight
            else 0
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }
    return keyboardState
}

