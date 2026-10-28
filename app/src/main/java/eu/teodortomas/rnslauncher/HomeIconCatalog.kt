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
 * must not be filtered.
 */
internal data class HomeIcon(val key: String, val res: Int, val tinted: Boolean)

/**
 * The icon catalogue behind the six home keys.
 *
 * Labels are deliberately not part of it. A name nobody has to translate lives in
 * [HomeMenuTile] as the default, and a user's own name is stored per key in [Prefs],
 * so the picker is icon-only and stays language-neutral.
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
        HomeIcon("vw-radio", R.drawable.ic_radio_fm, tinted = false),
        HomeIcon("vw-navigation", R.drawable.ic_map_pin, tinted = false),
        HomeIcon(KEY_APP_CONNECT, R.drawable.android_auto_launcher_icon_monochrome, tinted = false),
        HomeIcon("radio", R.drawable.ic_key_radio, tinted = true),
        HomeIcon("music", R.drawable.ic_key_music, tinted = true),
        HomeIcon("mic", R.drawable.ic_key_mic, tinted = true),
        HomeIcon("movie", R.drawable.ic_key_movie, tinted = true),
        HomeIcon("camera", R.drawable.ic_key_camera, tinted = true),
        HomeIcon("photo", R.drawable.ic_key_photo, tinted = true),
        HomeIcon("call", R.drawable.ic_key_call, tinted = true),
        HomeIcon("smartphone", R.drawable.ic_key_smartphone, tinted = true),
        HomeIcon("contacts", R.drawable.ic_key_contacts, tinted = true),
        HomeIcon("message", R.drawable.ic_key_message, tinted = true),
        HomeIcon("email", R.drawable.ic_key_email, tinted = true),
        HomeIcon("navigation", R.drawable.ic_key_navigation, tinted = true),
        HomeIcon("explore", R.drawable.ic_key_explore, tinted = true),
        HomeIcon("map", R.drawable.ic_key_map, tinted = true),
        HomeIcon("gps", R.drawable.ic_key_gps, tinted = true),
        HomeIcon("apps", R.drawable.ic_key_apps, tinted = true),
        HomeIcon("settings", R.drawable.ic_key_settings, tinted = true),
        HomeIcon("clock", R.drawable.ic_key_clock, tinted = true),
        HomeIcon("calendar", R.drawable.ic_key_calendar, tinted = true),
        HomeIcon("weather", R.drawable.ic_key_weather, tinted = true),
        HomeIcon("fuel", R.drawable.ic_key_fuel, tinted = true),
        HomeIcon("ev", R.drawable.ic_key_ev, tinted = true),
        HomeIcon("car", R.drawable.ic_key_car, tinted = true),
        HomeIcon("climate", R.drawable.ic_key_climate, tinted = true),
        HomeIcon("parking", R.drawable.ic_key_parking, tinted = true),
        HomeIcon("service", R.drawable.ic_key_service, tinted = true),
        HomeIcon("bluetooth", R.drawable.ic_key_bluetooth, tinted = true),
        HomeIcon("wifi", R.drawable.ic_key_wifi, tinted = true),
        HomeIcon("equalizer", R.drawable.ic_key_equalizer, tinted = true),
        HomeIcon("tune", R.drawable.ic_key_tune, tinted = true),
        HomeIcon("speed", R.drawable.ic_key_speed, tinted = true),
        HomeIcon("browser", R.drawable.ic_key_browser, tinted = true),
        HomeIcon("download", R.drawable.ic_key_download, tinted = true),
        HomeIcon("store", R.drawable.ic_key_store, tinted = true),
        HomeIcon("shop", R.drawable.ic_key_shop, tinted = true),
        HomeIcon("shopping-bag", R.drawable.ic_key_shopping_bag, tinted = true),
        HomeIcon("get-app", R.drawable.ic_key_get_app, tinted = true)
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
