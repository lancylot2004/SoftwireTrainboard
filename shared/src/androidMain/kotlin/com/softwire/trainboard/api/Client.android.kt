package com.softwire.trainboard.api

import com.softwire.trainboard.structures.JourneyLeg
import com.softwire.trainboard.utilities.Config
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus

actual fun createHttpClient(): HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                serializersModule += JourneyLeg.Module
                classDiscriminator = "type"
            },
        )
    }
    install(HttpCache)
    install(DefaultRequest) {
        header("x-api-key", "\"${Config.API_KEY}\"")
    }
}
