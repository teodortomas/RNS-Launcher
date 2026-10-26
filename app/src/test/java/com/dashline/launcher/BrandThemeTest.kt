package com.dashline.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandThemeTest {

    @Test
    fun missingOrUnknownKeysFallBackToTheVolkswagenSkin() {
        assertEquals(BrandTheme.VOLKSWAGEN, BrandTheme.fromKey(null))
        assertEquals(BrandTheme.VOLKSWAGEN, BrandTheme.fromKey(""))
        assertEquals(BrandTheme.VOLKSWAGEN, BrandTheme.fromKey("seat"))
    }

    @Test
    fun savedKeysRoundTripAndAreUnique() {
        BrandTheme.entries.forEach { theme ->
            assertEquals(theme, BrandTheme.fromKey(theme.key))
        }
        assertEquals(
            BrandTheme.entries.size,
            BrandTheme.entries.map { it.key }.distinct().size
        )
    }

    @Test
    fun volkswagenAccentIsRedAndSkodaAccentIsGreen() {
        val vw = BrandTheme.VOLKSWAGEN.palette.accent
        val skoda = BrandTheme.SKODA.palette.accent

        assertTrue("VW accent must be red", channel(vw, 16) > 150)
        assertTrue("VW accent must not read green", channel(vw, 8) < channel(vw, 16) / 2)
        assertTrue("VW accent must not read blue", channel(vw, 0) < channel(vw, 16) / 2)

        assertTrue("Škoda accent must read green", channel(skoda, 8) > channel(skoda, 16) * 2)
        assertTrue("Škoda accent must not read red", channel(skoda, 8) > channel(skoda, 16))
        assertEquals("Škoda green from the marque's palette", 0xFF4BA82E.toInt(), skoda)
    }

    @Test
    fun skodaAccentStaysReadableOnTheBlackStage() {
        // Contrast against the #090A0B stage; large text only needs 3:1, but the
        // 1.2dp ring and the key rims are thin, so aim for body-text contrast.
        assertTrue(contrast(BrandTheme.SKODA.palette.accent, 0xFF090A0B.toInt()) >= 4.5)
    }

    @Test
    fun softAccentIsTheAccentAtPressedOpacity() {
        BrandTheme.entries.forEach { theme ->
            val palette = theme.palette
            assertEquals(0x35, palette.accentSoft ushr 24 and 0xFF)
            assertEquals(palette.accent and 0xFFFFFF, palette.accentSoft and 0xFFFFFF)
        }
    }

    @Test
    fun pressedKeysLeanTowardsTheirBrandHue() {
        BrandTheme.entries.forEach { theme ->
            val palette = theme.palette
            val accentLeansGreen = channel(palette.accent, 8) > channel(palette.accent, 16)
            listOf(palette.circlePressedTop, palette.circlePressedBottom).forEach { color ->
                assertEquals(
                    "${theme.key} pressed key must lean towards its accent",
                    accentLeansGreen,
                    channel(color, 8) > channel(color, 16)
                )
            }
            assertTrue(
                "${theme.key} pressed key must keep its vertical shading",
                luminance(palette.circlePressedBottom) < luminance(palette.circlePressedTop)
            )
        }
    }

    private fun channel(color: Int, shift: Int): Int = color ushr shift and 0xFF

    private fun luminance(color: Int): Double {
        fun linear(value: Int): Double {
            val channel = value / 255.0
            return if (channel <= 0.03928) channel / 12.92
            else Math.pow((channel + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * linear(channel(color, 16)) +
            0.7152 * linear(channel(color, 8)) +
            0.0722 * linear(channel(color, 0))
    }

    private fun contrast(a: Int, b: Int): Double {
        val first = luminance(a)
        val second = luminance(b)
        return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
    }
}
