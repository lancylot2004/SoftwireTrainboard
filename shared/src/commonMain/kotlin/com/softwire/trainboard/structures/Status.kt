package com.softwire.trainboard.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    @SerialName("normal")
    NORMAL,

    @SerialName("delayed")
    DELAYED,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("fully_reserved")
    FULLY_RESERVED,

    ;

    override fun toString(): String {
        val correctCasedEnum = name.lowercase().replaceFirstChar { it.uppercaseChar() }
        return correctCasedEnum.replace('_', ' ')
    }
}
