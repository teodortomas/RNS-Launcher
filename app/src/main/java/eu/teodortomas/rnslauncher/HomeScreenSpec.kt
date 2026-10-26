package eu.teodortomas.rnslauncher

/** Normalized positions for the six circular launcher keys, arranged like the supplied RNS reference. */
internal data class RadialMenuPosition(
    val tile: HomeMenuTile,
    val xPermille: Int,
    val yPermille: Int
)

internal object HomeScreenSpec {
    const val logoFractionPermille = 560
    const val captionSizeSp = 10f
    const val circleAccentStrokeDp = 2
    const val circleDarkRimDp = 4
    const val centerClockSizeSp = 120f
    const val centerClockMinSizeSp = 60f
    const val centerDateSizeSp = 19f
    const val centerDateMinSizeSp = 14f
    const val centerSpeedSizeSp = 76f
    const val centerSpeedMinSizeSp = 38f
    const val centerSpeedUnitSizeSp = 22f
    const val centerLabelWidthPermille = 650
    const val centerLabelMaxHeightDp = 320
    const val ellipseRadiusXPermille = 395
    const val ellipseRadiusYPermille = 465

    fun logoSizeForButton(buttonSizePx: Int): Int =
        buttonSizePx * logoFractionPermille / 1_000

    fun logoInsetForButton(buttonSizePx: Int): Int =
        (buttonSizePx - logoSizeForButton(buttonSizePx)) / 2

    val buttons = listOf(
        RadialMenuPosition(HomeMenuTile.RADIO, 273, 120),
        RadialMenuPosition(HomeMenuTile.MEDIA, 727, 120),
        RadialMenuPosition(HomeMenuTile.PHONE, 123, 360),
        RadialMenuPosition(HomeMenuTile.NAVIGATION, 877, 360),
        RadialMenuPosition(HomeMenuTile.APP_CONNECT, 143, 700),
        RadialMenuPosition(HomeMenuTile.APPS, 857, 700)
    )
}
