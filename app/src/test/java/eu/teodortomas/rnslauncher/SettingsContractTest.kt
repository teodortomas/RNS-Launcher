package eu.teodortomas.rnslauncher

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
                "rowTheme",
                "rowKeepOn",
                "rowHomeKeys",
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
    fun themeRowSwitchesBetweenTheBrandSkins() {
        val settings = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/SettingsActivity.kt"
        ).readText()
        assertTrue(
            "rowTheme must open the theme dialog",
            settings.contains("binding.rowTheme.setOnClickListener { showThemeDialog() }")
        )
        assertTrue("the pick must be persisted", settings.contains("prefs.brandTheme ="))
        assertTrue(
            "both brand skins must be offered",
            settings.contains("BrandTheme.entries")
        )
        assertTrue(BrandTheme.entries.containsAll(listOf(BrandTheme.VOLKSWAGEN, BrandTheme.SKODA)))
    }

    @Test
    fun appRowsLaunchTheRolesUsedByTheCircularHomeTiles() {
        val settings = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/SettingsActivity.kt"
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

    /**
     * A key can be renamed on the dashboard. The settings row that picks its app and
     * the picker's own header must say the same name, or the user is configuring a
     * button that no longer exists under that label.
     */
    @Test
    fun appRowsAndThePickerNameTheKeyAsTheHomeScreenDoes() {
        val settings = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/SettingsActivity.kt"
        ).readText()
        val picker = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/AppPickerActivity.kt"
        ).readText()
        assertTrue("settings rows must use the key caption", settings.contains("KeyLabels.caption"))
        assertTrue("the picker header must use the key caption", picker.contains("KeyLabels.caption"))
    }

    @Test
    fun appPickerUsesTheSameFixedVwPaletteAsSettings() {
        val activity = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/AppPickerActivity.kt"
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
    fun appDrawerUsesTheSameFixedVwPaletteAsSettingsAndPicker() {
        val activity = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/AppDrawerActivity.kt"
        ).readText()
        val layout = findProjectFile("app/src/main/res/layout/activity_app_drawer.xml").readText()
        assertTrue(activity.contains("override val usesUserGradient: Boolean = false"))
        assertTrue(layout.contains("@drawable/vw_settings_background"))
        assertTrue(layout.contains("@color/vw_settings_accent"))
        assertTrue(layout.contains("@color/vw_settings_primary"))
        // Tiles and the search field carry the selected brand, not the stock red
        // baked into the XML drawables.
        assertTrue(activity.contains("BrandChrome.tileBackground"))
        assertTrue(activity.contains("BrandChrome.inputBackground"))
    }

    @Test
    fun volkswagenSkinKeepsTheSameRedTheLayoutsSpell() {
        // BrandChrome.tintAccent() compares finished views against the stock red in
        // colors.xml, so if the palette and the resource drift apart the Škoda skin
        // silently keeps red chrome. Pin the two together.
        val colors = findProjectFile("app/src/main/res/values/colors.xml").readText()
        val stock = Regex("""<color name="vw_settings_accent">#([0-9A-Fa-f]{6})</color>""")
            .find(colors)?.groupValues?.get(1)?.uppercase()
            ?: error("vw_settings_accent missing from colors.xml")
        val theme = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/BrandTheme.kt"
        ).readText()
        assertTrue(
            "BrandTheme.VOLKSWAGEN must use #$stock like @color/vw_settings_accent",
            theme.contains("0xFF$stock")
        )
    }

    @Test
    fun circularHomeAppliesAndClearsTheKeepScreenOnPreference() {
        val source = findProjectFile(
            "app/src/main/java/eu/teodortomas/rnslauncher/HomeActivity.kt"
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
