package eu.teodortomas.rnslauncher

import kotlin.math.roundToInt

/** Formats platform GPS speed (meters/second) for the launcher display. */
internal object GpsSpeedFormatter {
    const val UNIT = "km/h"
    const val NO_FIX_VALUE = "--"

    fun valueFromMetersPerSecond(speed: Float?): String {
        val metersPerSecond = speed?.takeIf { it.isFinite() && it >= 0f } ?: return NO_FIX_VALUE
        return (metersPerSecond * 3.6f).roundToInt().toString()
    }
}
