package com.softwire.trainboard.structures

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@OptIn(ExperimentalTime::class)
data class Journey(
    val journeyOptionToken: String,
    val journeyId: String,
    val originStation: Station,
    val destinationStation: Station,
    val departureTime: @Contextual Instant,
    val arrivalTime: @Contextual Instant,
    val status: Status,
    val legs: List<JourneyLeg>,
    val tickets: List<Ticket>,
    val journeyDurationInMinutes: Int,
    val isFastestJourney: Boolean,
)
