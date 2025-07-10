package com.example.trainboard.api

import com.example.trainboard.BuildConfig
import com.example.trainboard.structures.Station
import com.example.trainboard.structures.StationsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI

object Client {
    private val URL_BASE = URI("https://int-test1.tram.softwire-lner-dev.co.uk/v1/")

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> get() = _stations

    init {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                return@runCatching client
                    .get(URL_BASE.resolve("stations").toString())
                    .body<StationsResponse>()
                    .stations
                    .filter { it.crs != null }
            }.fold(
                onSuccess = { _stations.value = it },
                onFailure = { /* TODO: Errors won't cause crashes, but are better handled in part two. */ },
            )
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        install(HttpCache)
        install(DefaultRequest) {
            header("x-api-key", BuildConfig.API_KEY)
        }
    }
}
