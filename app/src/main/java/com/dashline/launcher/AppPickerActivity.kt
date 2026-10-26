// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package com.dashline.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.dashline.launcher.databinding.ActivityAppPickerBinding

/**
 * Full-screen app chooser for a given "role" (phone / nav / radio / bluetooth).
 * Saves the selection to Prefs and returns the chosen package to the caller.
 */
class AppPickerActivity : BaseActivity() {

    override val usesUserGradient: Boolean = false

    private lateinit var binding: ActivityAppPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val palette = BrandTheme.current(this).palette
        binding.header.root.setBackgroundColor(ContextCompat.getColor(this, R.color.vw_settings_surface))
        binding.header.pageTitle.setTextColor(ContextCompat.getColor(this, R.color.vw_settings_primary))
        binding.header.navBack.setColorFilter(palette.accent)
        binding.header.navHome.setColorFilter(palette.accent)
        BrandChrome.tintAccent(binding.root, palette)

        val role = intent.getStringExtra(EXTRA_ROLE) ?: Prefs.ROLE_PHONE
        binding.header.pageTitle.text = titleForRole(role)
        bindPageNav(binding.header.navBack, binding.header.navHome)

        val prefs = Prefs(this)
        val apps = AppRepository.loadLaunchableApps(this)
        val adapter = AppAdapter(
            apps,
            onClick = { app ->
                prefs.setForRole(role, app.packageName)
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(EXTRA_PACKAGE, app.packageName)
                    putExtra(EXTRA_ROLE, role)
                })
                finish()
            },
            // Tiles are recycled, so hand each one its own brand-tinted selector.
            itemBackground = { BrandChrome.tileBackground(this, palette) }
        )

        binding.pickerGrid.layoutManager = GridLayoutManager(this, 5)
        binding.pickerGrid.adapter = adapter
        binding.pickerGrid.setHasFixedSize(true)
    }

    private fun titleForRole(role: String): String = when (role) {
        Prefs.ROLE_PHONE -> getString(R.string.settings_phone_app)
        Prefs.ROLE_NAV -> getString(R.string.settings_nav_app)
        Prefs.ROLE_RADIO -> getString(R.string.settings_radio_app)
        Prefs.ROLE_MEDIA -> getString(R.string.settings_media_app)
        Prefs.ROLE_ANDROID_AUTO -> getString(R.string.settings_android_auto_app)
        Prefs.ROLE_CARPLAY -> getString(R.string.settings_carplay_app)
        Prefs.ROLE_FAVORITE -> getString(R.string.pick_favorite)
        else -> getString(R.string.pick_an_app)
    }

    companion object {
        const val EXTRA_ROLE = "extra_role"
        const val EXTRA_PACKAGE = "extra_package"

        fun start(activity: Activity, role: String, requestCode: Int) {
            val intent = Intent(activity, AppPickerActivity::class.java)
                .putExtra(EXTRA_ROLE, role)
            activity.startActivityForResult(intent, requestCode)
        }
    }
}
