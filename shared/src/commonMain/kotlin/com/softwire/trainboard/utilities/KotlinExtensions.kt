package com.softwire.trainboard.utilities

import kotlin.math.pow
import kotlin.math.round

fun Double.roundToDecimalPlaces(places: Int): String {
    val multiplier = 10.0.pow(places)
    val rounded = round(this * multiplier) / multiplier
    return buildString {
        append(rounded.toString())
        if (!contains('.')) append('.')

        val indexOfDot = indexOf('.')
        while (length - indexOfDot <= 2) {
            append('0')
        }
    }
}
