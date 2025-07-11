package com.softwire.trainboard

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.window.ComposeUIViewController
import com.softwire.trainboard.ui.RootView
import com.softwire.trainboard.ui.theme.TrainBoardTheme

@Suppress("unused")
fun MainViewController() = ComposeUIViewController {
    TrainBoardTheme {
        RootView(Modifier.fillMaxSize())
    }
}
