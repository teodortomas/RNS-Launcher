// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import android.content.Context

/**
 * The colours that change with the car brand. Everything else on the circular
 * home screen — the black stage, the silver keys, the white type — is shared,
 * so a brand is only its accent plus the tints derived from it.
 */
data class BrandPalette(
    /** Ring, speed readout, key rims, icon tints and the settings accent bar. */
    val accent: Int,
    /** Translucent accent behind pressed chrome (settings key, list rows). */
    val accentSoft: Int,
    /** A pressed silver key: tinted face top, deeper face bottom, bright rim. */
    val circlePressedTop: Int,
    val circlePressedBottom: Int,
    val circlePressedBorder: Int
)

/**
 * The brand skins the launcher can wear.
 *
 * Volkswagen wears GTI red, the accent VW reserves for its performance models.
 * The value it replaced, #C43A43, was sampled from a photo of the RNS panel and
 * reads washed out: same hue, but a third less saturated, which no VW material
 * scores that low. Škoda uses the greens from the marque's own palette: #4BA82E
 * Škoda green for the accent, #78FAAE (Electric Green, the second of the two
 * greens in Škoda's 2022 corporate identity) for the pressed rim, and
 * desaturated tints of the same green on the pressed silver keys.
 */
enum class BrandTheme(val key: String, val palette: BrandPalette) {
    VOLKSWAGEN(
        key = Prefs.BRAND_VW,
        palette = BrandPalette(
            accent = 0xFFCB2026.toInt(),
            accentSoft = 0x35CB2026,
            circlePressedTop = 0xFFD4B7BA.toInt(),
            circlePressedBottom = 0xFF9C5B61.toInt(),
            circlePressedBorder = 0xFFE56C73.toInt()
        )
    ),
    SKODA(
        key = Prefs.BRAND_SKODA,
        palette = BrandPalette(
            accent = 0xFF4BA82E.toInt(),
            accentSoft = 0x354BA82E,
            circlePressedTop = 0xFFB7D3AE.toInt(),
            circlePressedBottom = 0xFF7C9C6E.toInt(),
            circlePressedBorder = 0xFF78FAAE.toInt()
        )
    );

    companion object {
        /** Fresh installs keep the Volkswagen look the launcher was built for. */
        val DEFAULT = VOLKSWAGEN

        /** An unknown key (older build, hand-edited prefs) falls back to the default. */
        fun fromKey(key: String?): BrandTheme = entries.firstOrNull { it.key == key } ?: DEFAULT

        fun current(context: Context): BrandTheme = fromKey(Prefs(context).brandTheme)
    }
}
