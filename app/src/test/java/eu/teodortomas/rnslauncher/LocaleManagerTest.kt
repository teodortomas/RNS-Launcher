package eu.teodortomas.rnslauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleManagerTest {
    @Test
    fun CzechIsAvailableInTheInAppLanguagePicker() {
        val czech = LocaleManager.LANGUAGES.firstOrNull { it.code == "cs" }
        assertEquals("Čeština", czech?.name)
    }
}
