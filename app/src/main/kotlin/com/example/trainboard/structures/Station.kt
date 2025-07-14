package com.example.trainboard.structures

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Station(
    @JsonNames("name", "displayName")
    val name: String,
    val crs: String?,
) {
    @Serializable
    data class StationsResponse(
        val stations: List<Station>,
    )
}
