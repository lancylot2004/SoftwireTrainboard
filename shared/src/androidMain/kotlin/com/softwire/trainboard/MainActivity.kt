package com.softwire.trainboard

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.softwire.trainboard.api.Client
import com.softwire.trainboard.ui.RootView
import com.softwire.trainboard.ui.theme.TrainBoardTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LaunchedEffect(Unit) {
                Client.fetchStations()
            }

            TrainBoardTheme {
                RootView(Modifier.fillMaxSize())
            }
        }
    }
}
