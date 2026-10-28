// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import android.content.Context

/**
 * What a home key says about itself. The dashboard draws it and the key editor
 * lists it, so both read the same values instead of each resolving the caption
 * and the app behind the key on its own.
 */
internal object KeyLabels {

    /** The caption under the key: the user's own name, or the built-in one. */
    fun caption(context: Context, prefs: Prefs, tile: HomeMenuTile): String =
        prefs.keyLabel(tile.id) ?: context.getString(tile.titleRes)

    /** The app the key opens, or the hint shown while none is picked yet. */
    fun appName(context: Context, prefs: Prefs, tile: HomeMenuTile): String {
        val role = tile.appRole ?: return context.getString(R.string.all_apps)
        val selected = prefs.candidatesFor(role)
            .firstOrNull { AppRepository.isInstalled(context, it) }
        return selected?.let { AppRepository.labelFor(context, it) }
            ?: context.getString(R.string.home_unassigned)
    }
}
