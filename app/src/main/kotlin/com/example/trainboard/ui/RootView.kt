package com.example.trainboard.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trainboard.structures.Station
import java.net.URI

private val URL_REDIRECT = URI(
    "https://www.lner.co.uk/" +
        "travel-information/travelling-now/live-train-times/depart/",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(modifier: Modifier = Modifier) {
    var departureStation by remember { mutableStateOf<Station?>(null) }
    var arrivalStation by remember { mutableStateOf<Station?>(null) }

    val uriHandler = LocalUriHandler.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier
            .padding(32.dp)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.Bottom,
            ),
        ) {
            StationDropdown(label = "From") { departureStation = it }
            StationDropdown(label = "To") { arrivalStation = it }

            TextButton(
                enabled = departureStation != null && arrivalStation != null,
                onClick = {
                    handleSearch(departureStation, arrivalStation, uriHandler)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Colour.primaryContainer,
                    contentColor = Colour.onPrimaryContainer,
                ),
            ) {
                Text(
                    text = "Search",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun handleSearch(
    fromStation: Station?,
    toStation: Station?,
    uriHandler: UriHandler,
) {
    requireNotNull(fromStation) { "Origin station not selected!" }
    requireNotNull(fromStation.crs) { "Origin station not valid!" }
    requireNotNull(toStation) { "Destination station not selected!" }
    requireNotNull(toStation.crs) { "Destination station not valid!" }
    uriHandler.openUri(
        URL_REDIRECT
            .resolve("${fromStation.crs}/")
            .resolve("${toStation.crs}/")
            .toString(),
    )
}
