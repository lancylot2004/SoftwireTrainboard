package com.softwire.trainboard.structures

import kotlinx.serialization.Serializable

@Serializable
data class FareSearchResult(
    val outboundJourneys: List<Journey>,
    val nextOutboundQuery: String?,
)
