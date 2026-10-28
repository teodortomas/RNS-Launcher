// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import eu.teodortomas.rnslauncher.databinding.ActivityHomeKeyBinding

/**
 * Editor for one home key: the caption it shows and the icon it wears.
 *
 * Both are saved as they are changed — a car screen should not ask the driver to
 * hunt for a Save button — and an empty caption or the key's own icon is stored
 * as "no choice", which is what makes "Reset" a two-line operation.
 */
class HomeKeyActivity : BaseActivity() {

    override val usesUserGradient: Boolean = false

    private lateinit var binding: ActivityHomeKeyBinding
    private lateinit var prefs: Prefs
    private lateinit var palette: BrandPalette
    private lateinit var tile: HomeMenuTile
    private lateinit var adapter: IconAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tile = HomeMenuTile.entries.firstOrNull { it.id == intent.getStringExtra(EXTRA_TILE_ID) }
            ?: HomeMenuTile.RADIO
        binding = ActivityHomeKeyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)
        palette = BrandTheme.current(this).palette

        binding.header.root.setBackgroundColor(ContextCompat.getColor(this, R.color.vw_settings_surface))
        // The slot's built-in name, not the caption: a renamed key should still say
        // which of the six it is.
        binding.header.pageTitle.setText(tile.titleRes)
        binding.header.pageTitle.setTextColor(ContextCompat.getColor(this, R.color.vw_settings_primary))
        binding.header.navBack.setColorFilter(palette.accent)
        binding.header.navHome.setColorFilter(palette.accent)
        bindPageNav(binding.header.navBack, binding.header.navHome)
        BrandChrome.tintAccent(binding.root, palette)
        BrandChrome.tintRows(binding.root, palette)

        binding.labelInput.background = BrandChrome.inputBackground(this, palette)
        binding.labelInput.filters = arrayOf(InputFilter.LengthFilter(MAX_LABEL))
        binding.labelInput.setText(prefs.keyLabel(tile.id) ?: "")
        binding.labelInput.hint = getString(tile.titleRes)
        binding.labelInput.doAfterTextChanged { text -> prefs.setKeyLabel(tile.id, text?.toString()) }

        adapter = IconAdapter(
            icons = HomeIcons.pickable,
            initialKey = HomeIcons.forTile(tile, prefs.keyIcon(tile.id)).key,
            palette = palette
        ) { icon ->
            prefs.setKeyIcon(tile.id, icon.key.takeIf { it != tile.defaultIconKey })
        }
        binding.iconGrid.layoutManager = GridLayoutManager(this, ICON_COLUMNS)
        binding.iconGrid.adapter = adapter
        binding.iconGrid.setHasFixedSize(true)

        binding.rowReset.setOnClickListener {
            prefs.clearKey(tile.id)
            binding.labelInput.setText("")
            adapter.select(HomeIcons.forTile(tile, null).key)
        }
    }

    companion object {
        private const val EXTRA_TILE_ID = "extra_tile_id"
        private const val ICON_COLUMNS = 9
        private const val MAX_LABEL = 12

        fun start(activity: Activity, tileId: String) {
            activity.startActivity(
                Intent(activity, HomeKeyActivity::class.java).putExtra(EXTRA_TILE_ID, tileId)
            )
        }
    }
}
