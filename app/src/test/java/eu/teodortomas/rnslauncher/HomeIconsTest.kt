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
    fun everyIconIsNamedAndNoNameIsUsedTwice() {
        val labels = HomeIcons.all.map { it.labelRes }
        assertEquals("two icons share a name", labels.size, labels.toSet().size)
        assertTrue("every icon needs a name", labels.none { it == 0 })
    }

    @Test
    fun pickerOffersEveryCatalogueIconExceptTheProjectionMark() {
        assertEquals(39, HomeIcons.pickable.size)
        assertTrue(HomeIcons.pickable.none { it.key == HomeIcons.KEY_APP_CONNECT })
    }

    /**
     * Material ships some glyphs under two names: `get_app` and `file_download` are
     * the same drawing, so adding both would offer one icon twice. Compare the path
     * data of every key drawable, not just its name.
     */
    @Test
    fun noTwoKeyDrawablesDrawTheSameGlyph() {
        val drawables = findResourceDirectory().resolve("drawable")
            .listFiles { file -> file.name.startsWith("ic_key_") && file.name.endsWith(".xml") }
            ?: error("no ic_key_* drawables found")
        val byGlyph = drawables.groupBy { file ->
            Regex("android:pathData=\"([^\"]*)\"")
                .findAll(file.readText())
                .joinToString("|") { it.groupValues[1] }
        }
        val duplicates = byGlyph.filterValues { it.size > 1 }
            .values.map { files -> files.map { it.name } }
        assertTrue("identical glyphs under different names: $duplicates", duplicates.isEmpty())
    }

    private fun findResourceDirectory(): java.io.File {
        var directory: java.io.File? =
            java.io.File(System.getProperty("user.dir") ?: ".").canonicalFile
        while (directory != null) {
            val moduleResources = java.io.File(directory, "src/main/res")
            if (java.io.File(moduleResources, "values/strings.xml").isFile) return moduleResources
            val appResources = java.io.File(directory, "app/src/main/res")
            if (java.io.File(appResources, "values/strings.xml").isFile) return appResources
            directory = directory.parentFile
        }
        error("Could not locate app/src/main/res from the test working directory")
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
