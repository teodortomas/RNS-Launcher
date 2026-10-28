package eu.teodortomas.rnslauncher

/** Main-screen actions; app-backed tiles resolve to the user's selected package. */
enum class HomeMenuTile(
    val id: String,
    val titleRes: Int,
    /** Default icon, as a [HomeIcons] key; the user may pick another. */
    val defaultIconKey: String,
    val appRole: String?
) {
    RADIO("radio", R.string.home_radio, "vw-radio", Prefs.ROLE_RADIO),
    MEDIA("media", R.string.home_media, "music", Prefs.ROLE_MEDIA),
    PHONE("phone", R.string.home_phone, "call", Prefs.ROLE_PHONE),
    NAVIGATION("navigation", R.string.home_navigation, "vw-navigation", Prefs.ROLE_NAV),
    APP_CONNECT("app-connect", R.string.home_app_connect, HomeIcons.KEY_APP_CONNECT, Prefs.ROLE_ANDROID_AUTO),
    APPS("apps", R.string.home_apps, "apps", null)
}
