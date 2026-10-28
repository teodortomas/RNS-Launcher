package eu.teodortomas.rnslauncher

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenSpecTest {
    @Test
    fun circularMenuPlacesAllSixActionsInThreeEvenlySpacedMirroredPairs() {
        val buttons = HomeScreenSpec.buttons

        assertEquals(HomeMenuTile.entries.size, buttons.size)
        assertEquals(HomeMenuTile.entries.toSet(), buttons.map { it.tile }.toSet())
        val rows = buttons.map { it.yPermille }.distinct().sorted()
        assertEquals(3, rows.size)
        assertEquals(rows[1] - rows[0], rows[2] - rows[1])
        assertTrue("rows are not centred on the display: $rows", rows[0] + rows[2] in 960..1_000)
        assertTrue(buttons.groupBy { it.yPermille }.values.all { pair ->
            pair.size == 2 && pair.sumOf { it.xPermille } == 1_000
        })
    }

    @Test
    fun circularMenuCentersStayInsideTheDisplayWithRoomForGreyButtons() {
        // The middle pair sits on the widest point of the ring, so it is allowed closer
        // to the edges than the other four.
        assertTrue(HomeScreenSpec.buttons.all { position ->
            position.xPermille in 100..900 && position.yPermille in 80..820
        })
    }

    @Test
    fun menuLogosOccupyMoreThanHalfOfEachGreyCircle() {
        assertEquals(56, HomeScreenSpec.logoSizeForButton(100))
    }

    @Test
    fun centralClockIsMuchLargerThanDateAndSpeed() {
        assertTrue(HomeScreenSpec.centerClockSizeSp >= 120f)
        assertTrue(HomeScreenSpec.centerClockSizeSp > HomeScreenSpec.centerDateSizeSp)
        assertTrue(HomeScreenSpec.centerClockSizeSp > HomeScreenSpec.centerSpeedSizeSp)
    }

    @Test
    fun dateGetsASmallIncreaseAndGpsSpeedGetsAProminentIncrease() {
        assertTrue(HomeScreenSpec.centerDateSizeSp >= 19f)
        assertTrue(HomeScreenSpec.centerSpeedSizeSp >= 76f)
        assertTrue(HomeScreenSpec.centerSpeedUnitSizeSp in 18f..28f)
    }

    @Test
    fun centralTextBlockIsWideEnoughForTheEnlargedClock() {
        assertTrue(HomeScreenSpec.centerLabelWidthPermille >= 650)
    }

    @Test
    fun largeTimeAndSpeedHaveReadableAutoFitFloors() {
        assertTrue(HomeScreenSpec.centerClockMinSizeSp >= 60f)
        assertTrue(HomeScreenSpec.centerClockMinSizeSp < HomeScreenSpec.centerClockSizeSp)
        assertTrue(HomeScreenSpec.centerSpeedMinSizeSp >= 38f)
        assertTrue(HomeScreenSpec.centerSpeedMinSizeSp < HomeScreenSpec.centerSpeedSizeSp)
    }

    @Test
    fun menuButtonCentersLieOnTheBackgroundEllipse() {
        val radiusX = HomeScreenSpec.ellipseRadiusXPermille.toFloat()
        val radiusY = HomeScreenSpec.ellipseRadiusYPermille.toFloat()
        HomeScreenSpec.buttons.forEach { position ->
            val normalizedX = (position.xPermille - 500) / radiusX
            val normalizedY = (position.yPermille - 500) / radiusY
            assertTrue(abs(normalizedX * normalizedX + normalizedY * normalizedY - 1f) < 0.01f)
        }
    }

    @Test
    fun menuCaptionsAreOnlySlightlyLargerThanTheirOriginalSize() {
        assertTrue(HomeScreenSpec.captionSizeSp > 9f)
        assertTrue(HomeScreenSpec.captionSizeSp <= 11f)
    }

    @Test
    fun circularButtonsHaveThickerAccentAndDarkGreyOutlines() {
        assertTrue(HomeScreenSpec.circleAccentStrokeDp >= 2)
        assertTrue(HomeScreenSpec.circleDarkRimDp >= 4)
    }
}
