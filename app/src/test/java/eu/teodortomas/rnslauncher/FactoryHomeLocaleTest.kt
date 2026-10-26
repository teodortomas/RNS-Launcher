package eu.teodortomas.rnslauncher

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FactoryHomeLocaleTest {
    @Test
    fun homeActivityAppliesSavedLocaleToItsBaseContext() {
        val source = findHomeActivitySource().readText()
        assertTrue(source.contains("override fun attachBaseContext(newBase: Context)"))
        assertTrue(source.contains("LocaleManager.applyTo(newBase)"))
    }

    @Test
    fun homeScreenRemovesHeaderFooterAndKeepsSettingsInTopRight() {
        val source = findHomeActivitySource().readText()
        assertFalse(source.contains("R.string.home_brand"))
        assertFalse(source.contains("createFooterBar"))
        assertFalse(source.contains("createFooterKey"))
        assertTrue(source.contains("Gravity.TOP or Gravity.END"))
        assertTrue(source.contains("R.drawable.ic_settings"))
        assertTrue(source.contains("SettingsActivity::class.java"))
    }

    private fun findHomeActivitySource(): File {
        var directory: File? = File(System.getProperty("user.dir") ?: ".").canonicalFile
        while (directory != null) {
            val candidate = File(
                directory,
                "app/src/main/java/eu/teodortomas/rnslauncher/FactoryHomeActivity.kt"
            )
            if (candidate.isFile) return candidate
            directory = directory.parentFile
        }
        error("Could not locate FactoryHomeActivity.kt from the test working directory")
    }
}
