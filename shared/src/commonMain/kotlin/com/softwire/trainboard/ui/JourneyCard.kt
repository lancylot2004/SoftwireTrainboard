package com.softwire.trainboard.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.softwire.trainboard.structures.Journey
import com.softwire.trainboard.utilities.Colour
import com.softwire.trainboard.structures.Station
import com.softwire.trainboard.utilities.HourMinuteFormatter
import com.softwire.trainboard.utilities.Padding
import com.softwire.trainboard.utilities.Typography
import com.softwire.trainboard.utilities.roundToDecimalPlaces
import kotlinx.datetime.format
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun JourneyCard(journey: Journey, modifier: Modifier = Modifier, earliestArrivalTime: String) {
    var isExpanded by remember { mutableStateOf(false) }

    val transferStations = getTransferStations(journey)
        .joinToString(separator = ", ") { it.name }

    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        animationSpec = tween(),
        label = "Journey Card Height Animation",
    )

    Card(
        modifier = modifier
            .height(cardHeight)
            .clickable(
                onClickLabel = if (isExpanded) "Collapse Journey Card" else "Expand Journey Card",
                role = Role.Button,
            ) { isExpanded = !isExpanded }
    ) {

        Column(
            Modifier.padding(Padding.Medium),
            verticalArrangement = Arrangement.Center,
        ) {

            DisplayTimesAndStations(
                startTime = journey.departureTime,
                endTime = journey.arrivalTime,
                journeyDuration = journey.journeyDurationInMinutes.minutes,
                startStation = journey.originStation.name,
                endStation = journey.destinationStation.name,
                isShowingStation = false,
                numberOfChanges = journey.legs.size - 1,
            )
            Row (modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically

            ) {
                if (journey.isFastestJourney) {
                    DisplayPills("Fastest", Color(0xFF006400))
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (journey.journeyId == earliestArrivalTime) {
                    DisplayPills("Arrives First", Color(0xFF673AB7))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = getCheapestTicketPrice(journey),
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.weight(1f))
                if (journey.legs.size > 1) {
                    Text(
                        text = "Change At: $transferStations",
                        style = Typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = "Current Status: ${journey.status.toString()}",
                    style = Typography.titleMedium
                )

            }
        }
    }
}

private fun getTransferStations(journey: Journey): MutableList<Station> {
    val transferStations = mutableListOf<Station>()
    journey.legs.indices.forEach { index ->
        if (index != journey.legs.lastIndex) {
            transferStations.add(journey.legs[index].destination)
        }
    }
    return transferStations
}

private fun getCheapestTicketPrice(journey : Journey): String {
    val cheapestTicket = journey
        .tickets
        .minByOrNull { it.priceInPennies }
    return if (cheapestTicket != null) {
        val cheapestTicketPrice: Double = cheapestTicket.priceInPennies/100.0
        val priceRounded = cheapestTicketPrice.roundToDecimalPlaces(2)
        "From £$priceRounded"
    } else {
        "No Tickets Available"
    }
}

@Composable
private fun DisplayPills(label: String, colour: Color) {
    Box(
        modifier = Modifier
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colour
            )
            .padding(horizontal = 12.dp, vertical = 0.5.dp)
    ) {
        Text(
            text = label,
            style = Typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DisplayTimesAndStations(
    startTime: Instant,
    endTime: Instant,
    journeyDuration: Duration,
    startStation: String,
    endStation: String,
    isShowingStation: Boolean,
    numberOfChanges: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DisplayTime(
            time = startTime,
            station = startStation,
            isShowing = isShowingStation,
        )

        ArrowWithDuration(journeyDuration, numberOfChanges)

        DisplayTime(
            time = endTime,
            station = endStation,
            isShowing = isShowingStation,
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DisplayTime(
    time: Instant,
    station: String,
    isShowing: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            time.format(HourMinuteFormatter),
            style = Typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (isShowing) {
            Text(
                station,
                style = Typography.labelMedium,
            )
        }
    }
}

@Composable
fun ArrowWithDuration(
    duration: Duration,
    numberOfChanges: Int,
) {
    val colour = Colour.onBackground

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = duration.toComponents { minutes, _, _ ->
                val hours = minutes / 60
                val hoursText = if (hours > 0) "${hours}h " else ""
                "$hoursText ${minutes % 60}m"
            },
            style = Typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )

        Canvas(
            Modifier
                .fillMaxWidth(.3f)
                .height(5.dp),
        ) {
            drawLine(
                color = colour,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 4f,
            )

            val headSize = 10f

            drawLine(
                color = colour,
                start = Offset(size.width, center.y),
                end = Offset(size.width - headSize, center.y - headSize),
                strokeWidth = 4f,
            )
            drawLine(
                color = colour,
                start = Offset(size.width, center.y),
                end = Offset(size.width - headSize, center.y + headSize),
                strokeWidth = 4f,
            )
        }

        Text(
            text = if (numberOfChanges == 0) "Direct" else "Changes: $numberOfChanges",
            style = Typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
