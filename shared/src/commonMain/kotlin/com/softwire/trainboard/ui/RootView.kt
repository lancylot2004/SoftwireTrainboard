package com.softwire.trainboard.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import com.softwire.trainboard.api.Client
import com.softwire.trainboard.structures.FareSearchResult
import com.softwire.trainboard.structures.Station
import com.softwire.trainboard.utilities.Colour
import com.softwire.trainboard.utilities.LoadState
import com.softwire.trainboard.utilities.Padding
import com.softwire.trainboard.utilities.Typography
import com.softwire.trainboard.utilities.applyIf
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(modifier: Modifier = Modifier) {
    var departureStation by remember { mutableStateOf<Station?>(null) }
    var arrivalStation by remember { mutableStateOf<Station?>(null) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchState: LoadState<FareSearchResult, String> by remember {
        mutableStateOf(LoadState.Idle)
    }

    val lazyListState = remember { LazyListState() }
    var isLoadingMore by remember { mutableStateOf(false) }

    Box(
        modifier
            .padding(Padding.Large)
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            },
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = Padding.Small,
                alignment = Alignment.Bottom,
            ),
        ) {
            /* TODO: Reconsider coupling between `StationDropdown` and API calls, and consider
               how to show error states which are unrelated to search, neatly. */
            when (searchState) {
                is LoadState.Idle -> SearchResultIdle()
                is LoadState.Loading -> SearchResultLoading()
                is LoadState.Error -> SearchResultError(searchState as LoadState.Error<String>)
                is LoadState.Success -> SearchResultSuccess(
                    searchState = searchState as LoadState.Success<FareSearchResult>,
                    lazyListState = lazyListState,
                    isLoadingMore = isLoadingMore,
                    departureStation = departureStation,
                    arrivalStation = arrivalStation,
                    refreshCallback = callback@{
                        if (searchState !is LoadState.Success<FareSearchResult>) return@callback
                        val fareSearchResult =
                            (searchState as LoadState.Success<FareSearchResult>).data
                        scope.launch {
                            isLoadingMore = true
                            searchState = Client.getMoreJourneyFares(fareSearchResult)
                            isLoadingMore = false
                        }
                    },
                )
            }

            StationDropdown(label = "From") { it?.let { departureStation = it } }
            StationDropdown(label = "To") { it?.let { arrivalStation = it } }

            TextButton(
                onClick = {
                    scope.launch {
                        onSearch(
                            departureStation,
                            arrivalStation,
                            snackbarHostState,
                            focusManager,
                        ) { newState -> searchState = newState }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(searchState is LoadState.Loading) { this.shimmer() },
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

private suspend fun onSearch(
    departureStation: Station?,
    arrivalStation: Station?,
    snackbarHostState: SnackbarHostState,
    focusManager: FocusManager,
    callback: (LoadState<FareSearchResult, String>) -> Unit,
) {
    if (!checkCanSearch(departureStation, arrivalStation, snackbarHostState)) {
        return
    }

    focusManager.clearFocus()
    callback(LoadState.Loading)

    handleSearch(departureStation, arrivalStation) {
        callback(it)
    }
}

@OptIn(ExperimentalContracts::class)
private suspend fun checkCanSearch(
    fromStation: Station?,
    toStation: Station?,
    snackbarHostState: SnackbarHostState,
): Boolean {
    contract {
        returns(true) implies (fromStation != null && toStation != null)
    }

    if (fromStation == null) {
        snackbarHostState.showSnackbar(
            message = "Origin station not selected or invalid.",
            actionLabel = "OK",
            duration = SnackbarDuration.Short,
        )

        return false
    }

    if (toStation == null) {
        snackbarHostState.showSnackbar(
            message = "Destination station not selected or invalid.",
            actionLabel = "OK",
            duration = SnackbarDuration.Short,
        )

        return false
    }

    requireNotNull(fromStation.crs) { "[Impossible] Origin station does not have a CRS." }
    requireNotNull(toStation.crs) { "[Impossible] Destination station does not have a CRS." }

    if (fromStation == toStation) {
        snackbarHostState.showSnackbar(
            message = "Origin and destination stations cannot be the same.",
            actionLabel = "OK",
            duration = SnackbarDuration.Short,
        )

        return false
    }

    return true
}

private suspend fun handleSearch(
    fromStation: Station,
    toStation: Station,
    callback: (LoadState<FareSearchResult, String>) -> Unit,
) = Client
    .getJourneyFares(fromStation, toStation)
    .let(callback)
