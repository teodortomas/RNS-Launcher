package eu.teodortomas.rnslauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class GpsSpeedFormatterTest {
    @Test
    fun convertsGpsMetersPerSecondToRoundedKilometersPerHourValue() {
        assertEquals("36", GpsSpeedFormatter.valueFromMetersPerSecond(10f))
        assertEquals("37", GpsSpeedFormatter.valueFromMetersPerSecond(10.14f))
    }

    @Test
    fun reportsZeroWhenGpsFixShowsTheVehicleStopped() {
        assertEquals("0", GpsSpeedFormatter.valueFromMetersPerSecond(0f))
    }

    @Test
    fun reportsNoFixForMissingOrInvalidGpsSpeed() {
        assertEquals("--", GpsSpeedFormatter.valueFromMetersPerSecond(null))
        assertEquals("--", GpsSpeedFormatter.valueFromMetersPerSecond(Float.NaN))
        assertEquals("--", GpsSpeedFormatter.valueFromMetersPerSecond(-1f))
    }

    @Test
    fun exposesKilometersPerHourAsASeparateUnitLabel() {
        assertEquals("km/h", GpsSpeedFormatter.UNIT)
    }
}
