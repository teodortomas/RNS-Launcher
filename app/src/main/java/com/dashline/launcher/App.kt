// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package com.dashline.launcher

import android.app.Application
import android.content.Context

/** Applies the saved language and the launcher's fixed dark theme before any activity. */
class App : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.applyTo(base))
    }

    override fun onCreate() {
        super.onCreate()
        ThemeManager.apply()
    }
}
