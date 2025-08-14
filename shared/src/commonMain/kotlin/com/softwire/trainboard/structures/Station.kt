package com.softwire.trainboard.structures

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Station(
    @JsonNames("name", "displayName")
    val name: String,
    val crs: String,
) {
    @Serializable
    data class StationsResponse(
        @Serializable(with = FilteredSerializer::class)
        val stations: List<Station>,
    )

    object FilteredSerializer :
        JsonTransformingSerializer<List<Station>>(ListSerializer(Station.serializer())) {
        override fun transformDeserialize(element: JsonElement): JsonElement {
            val array = element.jsonArray
            val filtered = array.filter { it.jsonObject["crs"]?.jsonPrimitive?.isString == true }
            return JsonArray(filtered)
        }
    }
}
