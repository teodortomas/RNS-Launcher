// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package com.dashline.launcher

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dashline.launcher.databinding.ItemAppBinding

/**
 * RecyclerView adapter for app grids (drawer, frequent strip, picker).
 * [itemWidthDp] > 0 fixes each item's width — needed for the horizontal strip,
 * where the layout's match_parent width would otherwise fill the whole row.
 * [itemBackground] supplies a fresh tile background per view, which is how the
 * picker gets its brand-tinted pressed state.
 */
class AppAdapter(
    private var apps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: ((AppInfo) -> Unit)? = null,
    private val itemWidthDp: Int = 0,
    private val itemBackground: (() -> Drawable)? = null
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    inner class AppViewHolder(val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun update(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        itemBackground?.invoke()?.let { binding.root.background = it }
        if (itemWidthDp > 0) {
            val px = (itemWidthDp * parent.resources.displayMetrics.density).toInt()
            binding.root.layoutParams =
                ViewGroup.LayoutParams(px, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.appIcon.setImageDrawable(app.icon)
        holder.binding.appLabel.text = app.label
        holder.binding.root.setOnClickListener { onClick(app) }
        holder.binding.root.setOnLongClickListener {
            onLongClick?.invoke(app)
            onLongClick != null
        }
    }

    override fun getItemCount(): Int = apps.size
}
