package com.softwire.trainboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.softwire.trainboard.api.Client
import com.softwire.trainboard.ui.RootView
import com.softwire.trainboard.ui.theme.TrainBoardTheme

@Suppress("unused")
fun MainViewController() = ComposeUIViewController {
    LaunchedEffect(Unit) {
        Client.fetchStations()
    }

    TrainBoardTheme {
        RootView(Modifier.fillMaxSize())
    }
}
