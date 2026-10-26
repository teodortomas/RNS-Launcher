package eu.teodortomas.rnslauncher

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenSpecTest {
    @Test
    fun circularMenuPlacesAllSixActionsInThreeMirroredPairs() {
        val buttons = HomeScreenSpec.buttons

        assertEquals(HomeMenuTile.entries.size, buttons.size)
        assertEquals(HomeMenuTile.entries.toSet(), buttons.map { it.tile }.toSet())
        assertEquals(listOf(120, 360, 700), buttons.map { it.yPermille }.distinct().sorted())
        assertTrue(buttons.groupBy { it.yPermille }.values.all { pair ->
            pair.size == 2 && pair.sumOf { it.xPermille } == 1_000
        })
    }

    @Test
    fun circularMenuCentersStayInsideTheDisplayWithRoomForGreyButtons() {
        assertTrue(HomeScreenSpec.buttons.all { position ->
            position.xPermille in 120..880 && position.yPermille in 80..820
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
