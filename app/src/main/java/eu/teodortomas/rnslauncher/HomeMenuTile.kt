package eu.teodortomas.rnslauncher

/** Main-screen actions; app-backed tiles resolve to the user's selected package. */
enum class HomeMenuTile(
    val id: String,
    val titleRes: Int,
    val iconRes: Int,
    val appRole: String?
) {
    RADIO("radio", R.string.home_radio, R.drawable.ic_radio_fm, Prefs.ROLE_RADIO),
    MEDIA("media", R.string.home_media, R.drawable.ic_audio, Prefs.ROLE_MEDIA),
    PHONE("phone", R.string.home_phone, R.drawable.ic_phone, Prefs.ROLE_PHONE),
    NAVIGATION("navigation", R.string.home_navigation, R.drawable.ic_map_pin, Prefs.ROLE_NAV),
    APP_CONNECT("app-connect", R.string.home_app_connect, R.drawable.android_auto_launcher_icon_monochrome, Prefs.ROLE_ANDROID_AUTO),
    APPS("apps", R.string.home_apps, R.drawable.ic_apps, null)
}
