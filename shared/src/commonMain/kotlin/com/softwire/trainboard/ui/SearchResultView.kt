package com.softwire.trainboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.softwire.trainboard.structures.FareSearchResult
import com.softwire.trainboard.structures.Station
import com.softwire.trainboard.utilities.LoadState
import com.softwire.trainboard.utilities.Padding
import com.softwire.trainboard.utilities.Typography
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
internal fun SearchResultIdle() {
    Text(
        text = "Search for journeys between two stations.",
        style = Typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
internal fun ColumnScope.SearchResultLoading() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    KamelImage(
        resource = {
            asyncPainterResource(
                "https://media.licdn.com/dms/image/v2/D4E03AQGYKKntDnhkKA/profile-displayphoto-shrink_400_400/profile-displayphoto-shrink_400_400/0/1710953472462?e=1757548800&v=beta&t=Y3yA635XxMFmPnXJo0ffGWa0jgfr--COMEU9maR8A24",
            )
        },
        contentDescription = "Lancelot is loading your journeys...",
        modifier = Modifier
            .padding(Padding.Medium)
            .size(200.dp)
            .align(Alignment.CenterHorizontally)
            .graphicsLayer { rotationX = rotation }
            .clip(CircleShape),
    )

    KamelImage(
        resource = {
            asyncPainterResource(
                "https://media.licdn.com/dms/image/v2/D4E03AQHhzFaui6EW-A/profile-displayphoto-shrink_400_400/B4EZOWZvztH0Ag-/0/1733395152723?e=1757548800&v=beta&t=3LJSOy1MLupQeGQ5ldEAHvkyl17tsBtjfufAFJrX-n4",
            )
        },
        contentDescription = "Nick is loading your journeys...",
        modifier = Modifier
            .padding(Padding.Medium)
            .size(200.dp)
            .align(Alignment.CenterHorizontally)
            .graphicsLayer { rotationZ = rotation }
            .clip(CircleShape),
    )
}

@Composable
internal fun SearchResultError(searchState: LoadState.Error<String>) {
    Text(
        text = "An error occurred while searching for journeys: ${searchState.exception}",
        style = Typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.SearchResultSuccess(
    searchState: LoadState.Success<FareSearchResult>,
    lazyListState: LazyListState,
    isLoadingMore: Boolean,
    departureStation: Station?,
    arrivalStation: Station?,
    refreshCallback: () -> Unit,
) {
    val journeys = searchState.data.outboundJourneys

    if (journeys.isEmpty()) {
        Text(
            text = "No journeys found. Please try different stations.",
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        return
    }

    requireNotNull(departureStation) { "[Impossible] Departion station null after load." }
    requireNotNull(arrivalStation) { "[Impossible] Arrival station null after load." }

    Text(
        "${departureStation.name} to ${arrivalStation.name}",
        modifier = Modifier
            .padding(Padding.Medium)
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally),
        style = Typography.displaySmall,
        fontWeight = FontWeight.Bold,
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .pullToRefresh(
                isRefreshing = isLoadingMore,
                state = rememberPullToRefreshState(),
                threshold = (-40).dp,
                onRefresh = refreshCallback,
            ),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(
            space = Padding.Small,
            alignment = Alignment.Top,
        ),
    ) {
        items(journeys) { journey ->
            JourneyCard(journey)
        }

        if (journeys.size <= 5 && !isLoadingMore) {
            item {
                Icon(
                    imageVector = Lucide.ChevronDown,
                    contentDescription = "Load more journeys",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                )
            }
        }

        item {
            // TODO: We ran out of time so we're hard coding the "initial state" :)
            AnimatedVisibility(
                visible = isLoadingMore,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { CircularProgressIndicator() }
        }
    }
}
