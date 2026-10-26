// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package com.dashline.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.dashline.launcher.databinding.ActivitySettingsBinding

/** Launcher settings: change which app each role uses. */
class SettingsActivity : BaseActivity() {

    override val usesUserGradient: Boolean = false

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: Prefs
    private lateinit var palette: BrandPalette

    private companion object {
        const val REQ_DEFAULT_LAUNCHER = 10
        const val REQ_LOCATION = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)
        palette = BrandTheme.current(this).palette

        binding.header.root.setBackgroundColor(ContextCompat.getColor(this, R.color.vw_settings_surface))
        binding.header.pageTitle.setText(R.string.settings_title)
        binding.header.pageTitle.setTextColor(ContextCompat.getColor(this, R.color.vw_settings_primary))
        binding.header.navBack.setColorFilter(palette.accent)
        binding.header.navHome.setColorFilter(palette.accent)
        bindPageNav(binding.header.navBack, binding.header.navHome)

        // The layout spells the accent out as Volkswagen red; repaint it for the
        // brand that is actually selected.
        BrandChrome.tintAccent(binding.root, palette)
        BrandChrome.tintRows(binding.root, palette)

        binding.rowLanguage.setOnClickListener { showLanguageDialog() }
        binding.rowTheme.setOnClickListener { showThemeDialog() }
        binding.rowPhone.setOnClickListener {
            AppPickerActivity.start(this, Prefs.ROLE_PHONE, 1)
        }
        binding.rowNav.setOnClickListener {
            AppPickerActivity.start(this, Prefs.ROLE_NAV, 2)
        }
        binding.rowRadio.setOnClickListener {
            AppPickerActivity.start(this, Prefs.ROLE_RADIO, 3)
        }
        binding.rowMedia.setOnClickListener {
            AppPickerActivity.start(this, Prefs.ROLE_MEDIA, 5)
        }
        binding.rowAndroidAuto.setOnClickListener {
            AppPickerActivity.start(this, Prefs.ROLE_ANDROID_AUTO, 6)
        }
        binding.rowLocationAccess.setOnClickListener {
            when {
                Permissions.hasLocation(this) -> Unit
                Permissions.locationPermanentlyDenied(this) -> Permissions.openAppSettings(this)
                else -> Permissions.requestLocation(this, REQ_LOCATION)
            }
        }
        binding.rowKeepOn.setOnClickListener {
            prefs.keepScreenOn = !prefs.keepScreenOn
            refresh()
        }
        binding.rowDefaultLauncher.setOnClickListener {
            DefaultLauncher.request(this, REQ_DEFAULT_LAUNCHER)
        }
        binding.rowResetHidden.setOnClickListener {
            prefs.clearHiddenApps()
            refresh()
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()  // reflect default-launcher changes on return
    }

    private fun showLanguageDialog() {
        val codes = listOf("") + LocaleManager.LANGUAGES.map { it.code }
        val labels = (listOf(getString(R.string.language_system)) +
            LocaleManager.LANGUAGES.map { it.name }).toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_language)
            .setItems(labels) { _, which ->
                prefs.language = codes[which]
                // Relaunch the launcher so every screen re-inflates in the new language.
                val intent = Intent(this, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = BrandTheme.entries
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_theme)
            .setSingleChoiceItems(
                themes.map { getString(brandLabel(it)) }.toTypedArray(),
                themes.indexOf(BrandTheme.current(this))
            ) { dialog, which ->
                prefs.brandTheme = themes[which].key
                dialog.dismiss()
                // Accents are read once while the screen inflates, so rebuild it.
                recreate()
            }
            .show()
    }

    /** Localised name of a brand skin, e.g. "Škoda (green)". */
    private fun brandLabel(theme: BrandTheme): Int = when (theme) {
        BrandTheme.VOLKSWAGEN -> R.string.theme_volkswagen
        BrandTheme.SKODA -> R.string.theme_skoda
    }

    private fun refresh() {
        binding.languageValue.text = LocaleManager.displayName(this, prefs.language)
        binding.themeValue.text = getString(brandLabel(BrandTheme.current(this)))
        binding.phoneValue.text = label(prefs.phoneApp)
        binding.navValue.text = label(prefs.navApp)
        binding.radioValue.text = label(prefs.radioApp)
        binding.mediaValue.text = label(prefs.mediaApp)
        binding.androidAutoValue.text = label(prefs.androidAutoApp)
        binding.locationAccessValue.text = getString(
            if (Permissions.hasLocation(this)) R.string.access_granted
            else R.string.access_needed
        )
        binding.keepOnValue.text = getString(if (prefs.keepScreenOn) R.string.on else R.string.off)
        binding.defaultLauncherValue.text = getString(
            if (DefaultLauncher.isDefault(this)) R.string.yes else R.string.set_now
        )
        binding.resetHiddenValue.text = getString(R.string.hidden_count, prefs.hiddenApps.size)

        binding.versionValue.text = getString(
            R.string.settings_version_value,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
    }

    private fun label(pkg: String?): String =
        AppRepository.labelFor(this, pkg) ?: getString(R.string.not_set)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refresh()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
    }
}
