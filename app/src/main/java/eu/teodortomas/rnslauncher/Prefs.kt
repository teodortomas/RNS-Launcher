// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import android.content.Context

/**
 * Thin wrapper over SharedPreferences for what the launcher still configures:
 * which app each circular key opens, the language, the brand skin, and the two
 * display switches.
 *
 * The retired dashboard's preferences went with the screens that read them:
 * cards, widgets, tab order, the favourite dock, clock style, panel split and
 * the update check are no longer written, so their keys and helpers are gone.
 * Old values simply stay unread in SharedPreferences.
 */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("radio_launcher", Context.MODE_PRIVATE)

    /** Package name of the user's chosen phone app, or null if not set yet. */
    var phoneApp: String?
        get() = sp.getString(KEY_PHONE, null)
        set(v) = sp.edit().putString(KEY_PHONE, v).apply()

    /** Navigation app. Defaults to Google Maps per product decision. */
    var navApp: String
        get() = sp.getString(KEY_NAV, DEFAULT_NAV) ?: DEFAULT_NAV
        set(v) = sp.edit().putString(KEY_NAV, v).apply()

    var radioApp: String?
        get() = sp.getString(KEY_RADIO, null)
        set(v) = sp.edit().putString(KEY_RADIO, v).apply()

    /** App behind the media key. */
    var mediaApp: String?
        get() = sp.getString(KEY_MEDIA, null)
        set(v) = sp.edit().putString(KEY_MEDIA, v).apply()

    /** Phone-projection apps behind the Android Auto / CarPlay shortcuts. */
    var androidAutoApp: String?
        get() = sp.getString(KEY_AUTO, null)
        set(v) = sp.edit().putString(KEY_AUTO, v).apply()

    var carPlayApp: String?
        get() = sp.getString(KEY_CARPLAY, null)
        set(v) = sp.edit().putString(KEY_CARPLAY, v).apply()

    /**
     * Whether we've already shown the location prompt once, so a unit that never
     * got the chance is asked on a later launch rather than on every one.
     */
    var askedLocation: Boolean
        get() = sp.getBoolean(KEY_ASKED_LOCATION, false)
        set(v) = sp.edit().putBoolean(KEY_ASKED_LOCATION, v).apply()

    /** Language override as an ISO code (e.g. "tr"), or "" to follow the system. */
    var language: String
        get() = sp.getString(KEY_LANGUAGE, "") ?: ""
        set(v) = sp.edit().putString(KEY_LANGUAGE, v).apply()

    /**
     * Brand skin of the circular dashboard: BRAND_VW (default, GTI red) or
     * BRAND_SKODA (Škoda green). Only accents change — see [BrandTheme].
     */
    var brandTheme: String
        get() = sp.getString(KEY_BRAND, BRAND_VW) ?: BRAND_VW
        set(v) = sp.edit().putString(KEY_BRAND, v).apply()

    /** Id of the selected UI colour gradient (see GradientThemes.PRESETS). */
    var gradient: String
        get() = sp.getString(KEY_GRADIENT, "midnight") ?: "midnight"
        set(v) = sp.edit().putString(KEY_GRADIENT, v).apply()

    /** Keep the screen on while the dashboard is showing. */
    var keepScreenOn: Boolean
        get() = sp.getBoolean(KEY_KEEP_ON, true)
        set(v) = sp.edit().putBoolean(KEY_KEEP_ON, v).apply()

    // ---- home keys: per-key caption and icon -------------------------------

    /**
     * Caption chosen for a key, or null when the key still shows its built-in
     * name. Blank text is stored as "no choice" so an emptied field falls back
     * instead of leaving a key silently unlabelled.
     */
    fun keyLabel(tileId: String): String? =
        sp.getString(keyLabelPref(tileId), null)?.takeIf { it.isNotBlank() }

    fun setKeyLabel(tileId: String, label: String?) {
        val editor = sp.edit()
        if (label.isNullOrBlank()) editor.remove(keyLabelPref(tileId))
        else editor.putString(keyLabelPref(tileId), label.trim())
        editor.apply()
    }

    /** [HomeIcons] key of the icon chosen for a key, or null for the built-in one. */
    fun keyIcon(tileId: String): String? =
        sp.getString(keyIconPref(tileId), null)?.takeIf { it.isNotBlank() }

    fun setKeyIcon(tileId: String, iconKey: String?) {
        val editor = sp.edit()
        if (iconKey.isNullOrBlank()) editor.remove(keyIconPref(tileId))
        else editor.putString(keyIconPref(tileId), iconKey)
        editor.apply()
    }

    /** Drops both customisations of one key. */
    fun clearKey(tileId: String) {
        sp.edit().remove(keyLabelPref(tileId)).remove(keyIconPref(tileId)).apply()
    }

    /**
     * Snapshot of every key's caption and icon. HomeActivity stays alive while the
     * user edits them on another screen, so comparing this on resume tells it
     * whether the keys it already built are still current.
     */
    fun homeKeysSignature(): String = HomeMenuTile.entries.joinToString("|") { tile ->
        "${tile.id}=${keyLabel(tile.id)}~${keyIcon(tile.id)}"
    }

    // ---- hidden apps (from the drawer) -------------------------------------

    /** Defensive copy — the Set returned by SharedPreferences must not be mutated. */
    val hiddenApps: Set<String>
        get() = HashSet(sp.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet())

    fun hideApp(pkg: String) =
        sp.edit().putStringSet(KEY_HIDDEN, hiddenApps + pkg).apply()

    fun clearHiddenApps() = sp.edit().remove(KEY_HIDDEN).apply()

    /** Resolve the effective candidate list for a role (user choice first). */
    fun candidatesFor(role: String): List<String> = when (role) {
        ROLE_PHONE -> listOfNotNull(phoneApp) + Defaults.PHONE
        ROLE_NAV -> listOf(navApp) + Defaults.NAV
        ROLE_RADIO -> listOfNotNull(radioApp) + Defaults.RADIO
        ROLE_MEDIA -> listOfNotNull(mediaApp) + Defaults.MEDIA
        ROLE_ANDROID_AUTO -> listOfNotNull(androidAutoApp) + Defaults.ANDROID_AUTO
        ROLE_CARPLAY -> listOfNotNull(carPlayApp) + Defaults.CARPLAY
        else -> emptyList()
    }

    fun setForRole(role: String, pkg: String) {
        when (role) {
            ROLE_PHONE -> phoneApp = pkg
            ROLE_NAV -> navApp = pkg
            ROLE_RADIO -> radioApp = pkg
            ROLE_MEDIA -> mediaApp = pkg
            ROLE_ANDROID_AUTO -> androidAutoApp = pkg
            ROLE_CARPLAY -> carPlayApp = pkg
        }
    }

    companion object {
        const val DEFAULT_NAV = "com.google.android.apps.maps"

        private const val KEY_PHONE = "phone_app"
        private const val KEY_NAV = "nav_app"
        private const val KEY_RADIO = "radio_app"
        private const val KEY_MEDIA = "media_app"
        private const val KEY_AUTO = "android_auto_app"
        private const val KEY_CARPLAY = "carplay_app"
        private const val KEY_KEEP_ON = "keep_screen_on"
        private const val KEY_HIDDEN = "hidden_apps"
        private const val KEY_KEY_LABEL = "key_label_"
        private const val KEY_KEY_ICON = "key_icon_"

        private fun keyLabelPref(tileId: String) = KEY_KEY_LABEL + tileId
        private fun keyIconPref(tileId: String) = KEY_KEY_ICON + tileId
        private const val KEY_LANGUAGE = "language"
        private const val KEY_ASKED_LOCATION = "asked_location"
        private const val KEY_GRADIENT = "gradient"
        private const val KEY_BRAND = "brand_theme"

        /** Persisted keys of [BrandTheme]; changing them would reset the skin. */
        const val BRAND_VW = "vw"
        const val BRAND_SKODA = "skoda"

        const val ROLE_PHONE = "phone"
        const val ROLE_NAV = "nav"
        const val ROLE_RADIO = "radio"
        const val ROLE_MEDIA = "media"
        const val ROLE_ANDROID_AUTO = "android_auto"
        const val ROLE_CARPLAY = "carplay"
    }
}

/** Common package names to try when the user hasn't picked one for a role. */
object Defaults {
    val PHONE = listOf(
        "com.google.android.dialer",
        "com.android.dialer",
        "com.samsung.android.dialer"
    )
    val NAV = listOf(
        "com.google.android.apps.maps",
        "com.waze",
        "com.sygic.aura"
    )
    val RADIO = listOf(
        "com.mtk.fm",
        "com.microntek.radio",
        "com.android.fmradio",
        "com.rtk.fmradio"
    )
    val MEDIA = listOf(
        "com.spotify.music",
        "com.google.android.apps.youtube.music",
        "com.google.android.music",
        "com.maxmpz.audioplayer",
        "com.android.music"
    )

    /**
     * Phone projection. On head units this is usually a third-party "link" app
     * (ZLink, EasyConnected, AutoKit, Carbit) that provides both Android Auto and
     * CarPlay, rather than Google's own Android Auto — which most units can't run.
     * These are best-guess defaults; the user can pick their own in Settings.
     */
    val ANDROID_AUTO = listOf(
        "com.google.android.projection.gearhead",
        "com.zjinnova.zlink",
        "net.easyconn",
        "com.autokit.carlink",
        "com.carbit.link"
    )
    val CARPLAY = listOf(
        "com.zjinnova.zlink",
        "net.easyconn",
        "com.autokit.carlink",
        "com.carbit.link",
        "com.hzbhd.carplay"
    )
}
