// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

/**
 * One icon a home key can wear.
 *
 * [key] is what [Prefs] stores: drawable ids are not stable across builds, so a
 * persisted id would point at a different picture after any resource shuffle.
 * [tinted] marks the monochrome glyphs, which are drawn in the key's icon colour;
 * the hand-drawn VW glyphs and the Android Auto mark carry their own colours and
 * must not be filtered. [labelRes] names the picture in the picker — it describes
 * the drawing, not the key, so it stays the app's own string and never mixes with
 * the caption the user writes.
 */
internal data class HomeIcon(
    val key: String,
    val res: Int,
    val tinted: Boolean,
    val labelRes: Int
)

/**
 * The icon catalogue behind the six home keys.
 *
 * The labels here name the drawings; a key's own caption is a separate thing and
 * lives in [Prefs].
 */
internal object HomeIcons {

    /** Android Auto's mark: the built-in icon of the projection key, never offered in the picker. */
    const val KEY_APP_CONNECT = "app-connect"

    /**
     * Every icon, in picker order. The two `vw-` glyphs are the ones the launcher
     * shipped with and are kept selectable, so a key that was switched to a flat
     * glyph can be put back without a reset. The Material glyphs are Apache-2.0;
     * see the header of each `ic_key_*.xml`.
     */
    val all: List<HomeIcon> = listOf(
        HomeIcon("vw-radio", R.drawable.ic_radio_fm, tinted = false, labelRes = R.string.icon_vw_radio),
        HomeIcon("vw-navigation", R.drawable.ic_map_pin, tinted = false, labelRes = R.string.icon_vw_navigation),
        HomeIcon(KEY_APP_CONNECT, R.drawable.android_auto_launcher_icon_monochrome, tinted = false, labelRes = R.string.icon_app_connect),
        HomeIcon("radio", R.drawable.ic_key_radio, tinted = true, labelRes = R.string.icon_radio),
        HomeIcon("music", R.drawable.ic_key_music, tinted = true, labelRes = R.string.icon_music),
        HomeIcon("mic", R.drawable.ic_key_mic, tinted = true, labelRes = R.string.icon_mic),
        HomeIcon("movie", R.drawable.ic_key_movie, tinted = true, labelRes = R.string.icon_movie),
        HomeIcon("camera", R.drawable.ic_key_camera, tinted = true, labelRes = R.string.icon_camera),
        HomeIcon("photo", R.drawable.ic_key_photo, tinted = true, labelRes = R.string.icon_photo),
        HomeIcon("call", R.drawable.ic_key_call, tinted = true, labelRes = R.string.icon_call),
        HomeIcon("smartphone", R.drawable.ic_key_smartphone, tinted = true, labelRes = R.string.icon_smartphone),
        HomeIcon("contacts", R.drawable.ic_key_contacts, tinted = true, labelRes = R.string.icon_contacts),
        HomeIcon("message", R.drawable.ic_key_message, tinted = true, labelRes = R.string.icon_message),
        HomeIcon("email", R.drawable.ic_key_email, tinted = true, labelRes = R.string.icon_email),
        HomeIcon("navigation", R.drawable.ic_key_navigation, tinted = true, labelRes = R.string.icon_navigation),
        HomeIcon("explore", R.drawable.ic_key_explore, tinted = true, labelRes = R.string.icon_explore),
        HomeIcon("map", R.drawable.ic_key_map, tinted = true, labelRes = R.string.icon_map),
        HomeIcon("gps", R.drawable.ic_key_gps, tinted = true, labelRes = R.string.icon_gps),
        HomeIcon("apps", R.drawable.ic_key_apps, tinted = true, labelRes = R.string.icon_apps),
        HomeIcon("settings", R.drawable.ic_key_settings, tinted = true, labelRes = R.string.icon_settings),
        HomeIcon("clock", R.drawable.ic_key_clock, tinted = true, labelRes = R.string.icon_clock),
        HomeIcon("calendar", R.drawable.ic_key_calendar, tinted = true, labelRes = R.string.icon_calendar),
        HomeIcon("weather", R.drawable.ic_key_weather, tinted = true, labelRes = R.string.icon_weather),
        HomeIcon("fuel", R.drawable.ic_key_fuel, tinted = true, labelRes = R.string.icon_fuel),
        HomeIcon("ev", R.drawable.ic_key_ev, tinted = true, labelRes = R.string.icon_ev),
        HomeIcon("car", R.drawable.ic_key_car, tinted = true, labelRes = R.string.icon_car),
        HomeIcon("climate", R.drawable.ic_key_climate, tinted = true, labelRes = R.string.icon_climate),
        HomeIcon("parking", R.drawable.ic_key_parking, tinted = true, labelRes = R.string.icon_parking),
        HomeIcon("service", R.drawable.ic_key_service, tinted = true, labelRes = R.string.icon_service),
        HomeIcon("bluetooth", R.drawable.ic_key_bluetooth, tinted = true, labelRes = R.string.icon_bluetooth),
        HomeIcon("wifi", R.drawable.ic_key_wifi, tinted = true, labelRes = R.string.icon_wifi),
        HomeIcon("equalizer", R.drawable.ic_key_equalizer, tinted = true, labelRes = R.string.icon_equalizer),
        HomeIcon("tune", R.drawable.ic_key_tune, tinted = true, labelRes = R.string.icon_tune),
        HomeIcon("speed", R.drawable.ic_key_speed, tinted = true, labelRes = R.string.icon_speed),
        HomeIcon("browser", R.drawable.ic_key_browser, tinted = true, labelRes = R.string.icon_browser),
        HomeIcon("download", R.drawable.ic_key_download, tinted = true, labelRes = R.string.icon_download),
        HomeIcon("store", R.drawable.ic_key_store, tinted = true, labelRes = R.string.icon_store),
        HomeIcon("shop", R.drawable.ic_key_shop, tinted = true, labelRes = R.string.icon_shop),
        HomeIcon("shopping-bag", R.drawable.ic_key_shopping_bag, tinted = true, labelRes = R.string.icon_shopping_bag),
        HomeIcon("get-app", R.drawable.ic_key_get_app, tinted = true, labelRes = R.string.icon_get_app)
    )

    /** What the picker offers: 39 icons, nine columns by five rows, so it scrolls. */
    val pickable: List<HomeIcon> = all.filterNot { it.key == KEY_APP_CONNECT }

    fun find(key: String?): HomeIcon? =
        if (key.isNullOrEmpty()) null else all.firstOrNull { it.key == key }

    /** The icon a key draws: the user's choice, or the tile's built-in default. */
    fun forTile(tile: HomeMenuTile, chosenKey: String?): HomeIcon {
        find(chosenKey)?.let { return it }
        return requireNotNull(find(tile.defaultIconKey)) {
            "HomeMenuTile.${tile.name} names an icon missing from the catalogue: ${tile.defaultIconKey}"
        }
    }
}
