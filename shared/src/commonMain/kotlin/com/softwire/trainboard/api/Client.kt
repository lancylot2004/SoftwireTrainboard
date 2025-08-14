package com.softwire.trainboard.api

import com.softwire.trainboard.structures.FareSearchResult
import com.softwire.trainboard.structures.Station
import com.softwire.trainboard.utilities.LoadState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

expect fun createHttpClient(): HttpClient

object Client {
    private val BASE_URL = "https://int-test1.tram.softwire-lner-dev.co.uk/v1/"

    private val _stations = MutableStateFlow<LoadState<List<Station>, String>>(LoadState.Idle)
    val stations: StateFlow<LoadState<List<Station>, String>>
        get() = _stations
    private val client = createHttpClient()

    suspend fun fetchStations() {
        _stations.value = LoadState.Loading
        runCatching {
            client
                .get(BASE_URL) {
                    url { appendPathSegments("stations") }
                }.body<Station.StationsResponse>()
                .stations
                .filter { it.crs != null }
        }.onSuccess {
            _stations.value = LoadState.Success(it)
        }.onFailure { error ->
            _stations.value = LoadState.Error(error.toString())
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getJourneyFares(
        originStation: Station,
        destinationStation: Station,
    ): LoadState<FareSearchResult, String> = runCatching {
        client
            .get(BASE_URL) {
                url {
                    appendPathSegments("fares")
                    parameter("originStation", originStation.crs)
                    parameter("destinationStation", destinationStation.crs)
                    parameter("outboundDateTime", Clock.System.now().toString())
                    parameter("inboundDateTime", null as String?)
                    parameter("numberOfChildren", 0)
                    parameter("numberOfAdults", 1)
                    parameter("maxNumberOfChanges", 5)
                }
            }.body<FareSearchResult>()
    }.fold(
        onSuccess = { LoadState.Success(it) },
        onFailure = { LoadState.Error(it.toString()) },
    )

    suspend fun getMoreJourneyFares(
        currentResult: FareSearchResult,
    ): LoadState<FareSearchResult, String> = runCatching {
        if (currentResult.nextOutboundQuery == null) {
            return LoadState.Success(currentResult)
        }

        client
            .get("${BASE_URL}fares${currentResult.nextOutboundQuery}")
            .body<FareSearchResult>()
    }.fold(
        onSuccess = {
            val combinedJourneys = currentResult.outboundJourneys + it.outboundJourneys
            LoadState.Success(it.copy(outboundJourneys = combinedJourneys))
        },
        onFailure = { LoadState.Error(it.toString()) },
    )
}
