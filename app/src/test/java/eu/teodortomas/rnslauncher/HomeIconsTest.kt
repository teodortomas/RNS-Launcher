// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeIconsTest {

    @Test
    fun everyTileHasADefaultIconInTheCatalogue() {
        HomeMenuTile.entries.forEach { tile ->
            assertNotEquals(
                "${tile.name} names an icon missing from the catalogue",
                null,
                HomeIcons.find(tile.defaultIconKey)
            )
        }
    }

    @Test
    fun catalogueKeysAreUniqueAndSafeToPersist() {
        val keys = HomeIcons.all.map { it.key }
        assertEquals("duplicate catalogue key", keys.size, keys.toSet().size)
        keys.forEach { key ->
            assertTrue("a stored key must stay a plain slug: $key", Regex("[a-z0-9-]+").matches(key))
        }
    }

    @Test
    fun noTwoEntriesReuseOneDrawable() {
        val drawables = HomeIcons.all.map { it.res }
        assertEquals("two catalogue entries share a drawable", drawables.size, drawables.toSet().size)
    }

    @Test
    fun pickerOffersNineColumnsByFourRows() {
        assertEquals(36, HomeIcons.pickable.size)
        assertTrue(HomeIcons.pickable.none { it.key == HomeIcons.KEY_APP_CONNECT })
    }

    @Test
    fun anEmptyOrUnknownChoiceFallsBackToTheTileDefault() {
        HomeMenuTile.entries.forEach { tile ->
            assertEquals(tile.defaultIconKey, HomeIcons.forTile(tile, null).key)
            assertEquals(tile.defaultIconKey, HomeIcons.forTile(tile, "").key)
            assertEquals(tile.defaultIconKey, HomeIcons.forTile(tile, "no-such-icon").key)
        }
    }
}
