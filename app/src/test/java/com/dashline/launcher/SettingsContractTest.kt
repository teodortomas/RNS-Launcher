package com.dashline.launcher

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsContractTest {
    @Test
    fun settingsExposeOnlyControlsUsedByTheCurrentLauncher() {
        val document = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(findProjectFile("app/src/main/res/layout/activity_settings.xml"))
        val androidNs = "http://schemas.android.com/apk/res/android"
        val nodes = document.getElementsByTagName("*")
        val rows = (0 until nodes.length).mapNotNull { index ->
            val element = nodes.item(index) as? org.w3c.dom.Element ?: return@mapNotNull null
            element.getAttributeNS(androidNs, "id")
                .substringAfterLast('/')
                .takeIf { it.startsWith("row") }
        }.toSet()

        assertEquals(
            setOf(
                "rowLanguage",
                "rowKeepOn",
                "rowPhone",
                "rowNav",
                "rowRadio",
                "rowMedia",
                "rowAndroidAuto",
                "rowLocationAccess",
                "rowDefaultLauncher",
                "rowResetHidden",
                "rowVersion"
            ),
            rows
        )
    }

    @Test
    fun settingsUseDedicatedVolkswagenDarkSurfaceAndAccent() {
        val document = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(findProjectFile("app/src/main/res/layout/activity_settings.xml"))
        val androidNs = "http://schemas.android.com/apk/res/android"
        assertEquals(
            "@drawable/vw_settings_background",
            document.documentElement.getAttributeNS(androidNs, "background")
        )

        val styles = findProjectFile("app/src/main/res/values/themes.xml").readText()
        assertTrue(styles.contains("@color/vw_settings_accent"))
        assertTrue(styles.contains("@drawable/vw_settings_group"))
    }

    @Test
    fun appRowsLaunchTheRolesUsedByTheCircularHomeTiles() {
        val settings = findProjectFile(
            "app/src/main/java/com/dashline/launcher/SettingsActivity.kt"
        ).readText()
        val rowsByRole = listOf(
            Triple("rowPhone", "ROLE_PHONE", Prefs.ROLE_PHONE),
            Triple("rowNav", "ROLE_NAV", Prefs.ROLE_NAV),
            Triple("rowRadio", "ROLE_RADIO", Prefs.ROLE_RADIO),
            Triple("rowMedia", "ROLE_MEDIA", Prefs.ROLE_MEDIA),
            Triple("rowAndroidAuto", "ROLE_ANDROID_AUTO", Prefs.ROLE_ANDROID_AUTO)
        )
        rowsByRole.forEach { (row, roleConstant, role) ->
            assertTrue("$row must open the picker", settings.contains("binding.$row.setOnClickListener"))
            assertTrue(
                "$row must select $role",
                settings.contains("AppPickerActivity.start(this, Prefs.$roleConstant")
            )
            assertTrue("$role must exist on a home tile", HomeMenuTile.entries.any { it.appRole == role })
        }
    }

    @Test
    fun appPickerUsesTheSameFixedVwPaletteAsSettings() {
        val activity = findProjectFile(
            "app/src/main/java/com/dashline/launcher/AppPickerActivity.kt"
        ).readText()
        val layout = findProjectFile("app/src/main/res/layout/activity_app_picker.xml").readText()
        val item = findProjectFile("app/src/main/res/layout/item_app.xml").readText()
        assertTrue(activity.contains("override val usesUserGradient: Boolean = false"))
        assertTrue(layout.contains("@drawable/vw_settings_background"))
        assertTrue(layout.contains("@color/vw_settings_accent"))
        assertTrue(item.contains("@drawable/vw_settings_app_tile_selector"))
        assertTrue(item.contains("@color/vw_settings_primary"))
    }

    @Test
    fun circularHomeAppliesAndClearsTheKeepScreenOnPreference() {
        val source = findProjectFile(
            "app/src/main/java/com/dashline/launcher/FactoryHomeActivity.kt"
        ).readText()
        val resume = source.substringAfter("override fun onResume()").substringBefore("override fun onPause()")
        val pause = source.substringAfter("override fun onPause()").substringBefore("private fun applyKeepScreenOn()")
        assertTrue(source.contains("prefs.keepScreenOn"))
        assertTrue(source.contains("FLAG_KEEP_SCREEN_ON"))
        assertTrue(resume.contains("applyKeepScreenOn()"))
        assertTrue(pause.contains("clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)"))
    }

    private fun findProjectFile(relativePath: String): File {
        var directory: File? = File(System.getProperty("user.dir") ?: ".").canonicalFile
        while (directory != null) {
            val candidate = File(directory, relativePath)
            if (candidate.isFile) return candidate
            directory = directory.parentFile
        }
        error("Could not locate $relativePath from the test working directory")
    }
}
