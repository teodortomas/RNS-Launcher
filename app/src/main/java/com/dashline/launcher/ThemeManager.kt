// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package com.dashline.launcher

import androidx.appcompat.app.AppCompatDelegate

/** The VW launcher uses one fixed dark palette, matching the circular home screen. */
object ThemeManager {
    fun apply() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
