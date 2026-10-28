// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eu.teodortomas.rnslauncher.databinding.ItemHomeIconBinding

/**
 * Grid of the icons a home key may wear. Every cell repeats the key face, so a
 * choice is judged the way it will look on the dashboard rather than as a glyph
 * on a dark rectangle.
 */
internal class IconAdapter(
    private val icons: List<HomeIcon>,
    initialKey: String,
    private val palette: BrandPalette,
    private val onPick: (HomeIcon) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    private var selectedKey: String = initialKey

    inner class IconViewHolder(val binding: ItemHomeIconBinding) :
        RecyclerView.ViewHolder(binding.root)

    /** Moves the accent frame, e.g. after the editor resets the key. */
    fun select(key: String) {
        if (key == selectedKey) return
        val previous = icons.indexOfFirst { it.key == selectedKey }
        selectedKey = key
        val current = icons.indexOfFirst { it.key == selectedKey }
        if (previous >= 0) notifyItemChanged(previous)
        if (current >= 0) notifyItemChanged(current)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder =
        IconViewHolder(
            ItemHomeIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val icon = icons[position]
        val context = holder.binding.root.context
        val selected = icon.key == selectedKey

        holder.binding.keyFace.background = BrandChrome.keyFace(context, palette)
        // Frame the square around the key, not the whole column: a cell-wide frame
        // reads as a bar and collides with the page edge in the last column.
        holder.binding.cellFrame.background =
            if (selected) BrandChrome.selectedFrame(context, palette) else null
        holder.binding.iconImage.setImageResource(icon.res)
        if (icon.tinted) holder.binding.iconImage.setColorFilter(BrandChrome.KEY_GLYPH)
        else holder.binding.iconImage.clearColorFilter()

        holder.binding.iconLabel.text = context.getString(icon.labelRes)
        holder.binding.root.isSelected = selected
        // The name of the drawing is what a screen reader should say here, not the
        // caption of the key being edited.
        holder.binding.root.contentDescription = context.getString(icon.labelRes)
        holder.binding.root.setOnClickListener {
            onPick(icon)
            select(icon.key)
        }
    }

    override fun getItemCount(): Int = icons.size
}
