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
        assertEquals(HomeIcons.KEY_APP_CONNECT, HomeMenuTile.APP_CONNECT.defaultIconKey)
        assertEquals(
            R.drawable.android_auto_launcher_icon_monochrome,
            HomeIcons.forTile(HomeMenuTile.APP_CONNECT, null).res
        )
    }

    @Test
    fun radioTileUsesTheClassicFmRadioIcon() {
        assertEquals(R.drawable.ic_radio_fm, HomeIcons.forTile(HomeMenuTile.RADIO, null).res)
    }

    @Test
    fun navigationTileUsesAMapMarkerIcon() {
        assertEquals(R.drawable.ic_map_pin, HomeIcons.forTile(HomeMenuTile.NAVIGATION, null).res)
    }

    @Test
    fun hardwareFunctionTilesMapToSelectableAndroidApps() {
        assertEquals(Prefs.ROLE_RADIO, HomeMenuTile.RADIO.appRole)
        assertEquals(Prefs.ROLE_MEDIA, HomeMenuTile.MEDIA.appRole)
        assertEquals(Prefs.ROLE_PHONE, HomeMenuTile.PHONE.appRole)
        assertEquals(Prefs.ROLE_NAV, HomeMenuTile.NAVIGATION.appRole)
    }
}
