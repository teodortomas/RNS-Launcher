package eu.teodortomas.rnslauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeMenuTileTest {
    @Test
    fun factoryHomeShowsCoreTilesInExpectedOrder() {
        assertEquals(
            listOf("radio", "media", "phone", "navigation", "app-connect", "apps"),
            HomeMenuTile.entries.map { it.id }
        )
    }

    @Test
    fun appConnectTileUsesAndroidAutoRoleAndAppsTileHasNoAppRole() {
        assertEquals(Prefs.ROLE_ANDROID_AUTO, HomeMenuTile.APP_CONNECT.appRole)
        assertNull(HomeMenuTile.APPS.appRole)
    }

    @Test
    fun appConnectTileUsesTheAndroidAutoLauncherIcon() {
        assertEquals(R.drawable.android_auto_launcher_icon_monochrome, HomeMenuTile.APP_CONNECT.iconRes)
    }

    @Test
    fun radioTileUsesTheClassicFmRadioIcon() {
        assertEquals(R.drawable.ic_radio_fm, HomeMenuTile.RADIO.iconRes)
    }

    @Test
    fun navigationTileUsesAMapMarkerIcon() {
        assertEquals(R.drawable.ic_map_pin, HomeMenuTile.NAVIGATION.iconRes)
    }

    @Test
    fun hardwareFunctionTilesMapToSelectableAndroidApps() {
        assertEquals(Prefs.ROLE_RADIO, HomeMenuTile.RADIO.appRole)
        assertEquals(Prefs.ROLE_MEDIA, HomeMenuTile.MEDIA.appRole)
        assertEquals(Prefs.ROLE_PHONE, HomeMenuTile.PHONE.appRole)
        assertEquals(Prefs.ROLE_NAV, HomeMenuTile.NAVIGATION.appRole)
    }
}
