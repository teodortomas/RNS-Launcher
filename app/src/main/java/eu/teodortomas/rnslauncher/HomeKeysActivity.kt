// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import eu.teodortomas.rnslauncher.databinding.ActivityHomeKeysBinding
import eu.teodortomas.rnslauncher.databinding.ItemHomeKeyBinding

/**
 * Settings page listing the six home keys: the icon each one wears, the caption
 * under it and the app behind it. Editing happens one key at a time in
 * [HomeKeyActivity]; the rows are rebuilt on every resume so a change made there
 * is visible on return.
 */
class HomeKeysActivity : BaseActivity() {

    override val usesUserGradient: Boolean = false

    private lateinit var binding: ActivityHomeKeysBinding
    private lateinit var prefs: Prefs
    private lateinit var palette: BrandPalette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeKeysBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)
        palette = BrandTheme.current(this).palette

        binding.header.root.setBackgroundColor(ContextCompat.getColor(this, R.color.vw_settings_surface))
        binding.header.pageTitle.setText(R.string.settings_home_keys)
        binding.header.pageTitle.setTextColor(ContextCompat.getColor(this, R.color.vw_settings_primary))
        binding.header.navBack.setColorFilter(palette.accent)
        binding.header.navHome.setColorFilter(palette.accent)
        bindPageNav(binding.header.navBack, binding.header.navHome)

        BrandChrome.tintAccent(binding.root, palette)
        rebuild()
    }

    override fun onResume() {
        super.onResume()
        rebuild()
    }

    private fun rebuild() {
        binding.keyList.removeAllViews()
        HomeMenuTile.entries.forEachIndexed { index, tile ->
            if (index > 0) binding.keyList.addView(divider())
            val row = ItemHomeKeyBinding.inflate(layoutInflater, binding.keyList, false)
            val icon = HomeIcons.forTile(tile, prefs.keyIcon(tile.id))
            row.keyFace.background = BrandChrome.keyFace(this, palette)
            row.keyIcon.setImageResource(icon.res)
            if (icon.tinted) row.keyIcon.setColorFilter(BrandChrome.KEY_GLYPH)
            else row.keyIcon.clearColorFilter()
            row.keyName.text = KeyLabels.caption(this, prefs, tile)
            row.keyApp.text = KeyLabels.appName(this, prefs, tile)
            row.root.setOnClickListener { HomeKeyActivity.start(this, tile.id) }
            binding.keyList.addView(row.root)
        }
        // Rows are inflated after the base class tinted the chrome.
        BrandChrome.tintRows(binding.keyList, palette)
    }

    private fun divider(): View = View(this).apply {
        setBackgroundColor(ContextCompat.getColor(this@HomeKeysActivity, R.color.vw_settings_divider))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(1)
        ).apply { marginStart = dp(16) }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
