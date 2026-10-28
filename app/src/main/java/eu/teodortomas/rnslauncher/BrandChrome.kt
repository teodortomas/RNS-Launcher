// Copyright (C) 2026 Metehan Kaygısız
// SPDX-License-Identifier: GPL-3.0-only

package eu.teodortomas.rnslauncher

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * The settings-style screens are inflated from layouts whose accent colours are
 * still spelled out as Volkswagen red in XML. Their accent is therefore swapped
 * at runtime: this walks the finished view tree and repaints anything drawn in
 * the stock red, and builds the pressed-state drawables that no resource can
 * recolour for us.
 */
internal object BrandChrome {

    /**
     * The red the layouts and drawables default to. Read from resources rather
     * than repeated as a literal: a stale copy here would silently stop the
     * brand repaint, leaving the Škoda skin with red chrome. Kept equal to
     * [BrandTheme.VOLKSWAGEN] by a test.
     */
    private fun stockAccent(context: Context): Int =
        ContextCompat.getColor(context, R.color.vw_settings_accent)

    private const val ROW_RADIUS_DP = 8f

    /** Recolours section headers and the accent bar under the page header. */
    fun tintAccent(root: View, palette: BrandPalette) {
        val stock = stockAccent(root.context)
        root.forEachDescendant { view ->
            val background = view.background
            when {
                view is TextView && view.currentTextColor == stock ->
                    view.setTextColor(palette.accent)
                background is ColorDrawable && background.color == stock ->
                    view.setBackgroundColor(palette.accent)
            }
        }
    }

    /**
     * Gives every clickable row the brand's pressed tint. Settings rows are the
     * only clickable views on that screen with a state-list background, so the
     * type check is enough to find them without tagging each row by hand.
     */
    fun tintRows(root: View, palette: BrandPalette) {
        root.forEachDescendant { view ->
            if (view.isClickable && view.background is StateListDrawable) {
                view.background = rowBackground(view.resources.displayMetrics.density, palette)
            }
        }
    }

    /** Pressed/focused row fill for the settings list. */
    private fun rowBackground(density: Float, palette: BrandPalette): Drawable =
        StateListDrawable().apply {
            val radius = ROW_RADIUS_DP * density
            addState(
                intArrayOf(android.R.attr.state_pressed),
                rounded(palette.accentSoft, radius)
            )
            addState(
                intArrayOf(android.R.attr.state_focused),
                rounded(palette.accentSoft, radius)
            )
            addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
        }

    /**
     * Search field: the settings surface at rest, and the brand accent as its rim
     * once it holds focus. The XML default cannot carry the accent, because a
     * selector's stroke colour is baked in and cannot be recoloured per brand.
     */
    fun inputBackground(context: Context, palette: BrandPalette): Drawable {
        val density = context.resources.displayMetrics.density
        val radius = ROW_RADIUS_DP * density
        val stroke = density.toInt().coerceAtLeast(1)
        val surface = ContextCompat.getColor(context, R.color.vw_settings_surface)
        val outline = ContextCompat.getColor(context, R.color.vw_settings_outline)
        return StateListDrawable().apply {
            addState(
                intArrayOf(android.R.attr.state_focused),
                rounded(surface, radius, stroke, palette.accent)
            )
            addState(intArrayOf(), rounded(surface, radius, stroke, outline))
        }
    }

    /** Pressed/focused tile of the app picker: accent rim, brand-tinted fill. */
    fun tileBackground(context: Context, palette: BrandPalette): Drawable {
        val density = context.resources.displayMetrics.density
        val radius = ROW_RADIUS_DP * density
        val stroke = density.toInt().coerceAtLeast(1)
        val surface = ContextCompat.getColor(context, R.color.vw_settings_surface)
        val outline = ContextCompat.getColor(context, R.color.vw_settings_outline)
        return StateListDrawable().apply {
            addState(
                intArrayOf(android.R.attr.state_pressed),
                rounded(palette.accentSoft, radius, stroke, palette.accent)
            )
            addState(
                intArrayOf(android.R.attr.state_focused),
                rounded(palette.accentSoft, radius, stroke, palette.accent)
            )
            addState(intArrayOf(), rounded(surface, radius, stroke, outline))
        }
    }

    /** The dark grey every flat key glyph is drawn in, on the silver face. */
    const val KEY_GLYPH = 0xFF424649.toInt()

    private const val KEY_EDGE = 0xFF555A5D.toInt()
    private const val KEY_TOP = 0xFFE0E2E3.toInt()
    private const val KEY_BOTTOM = 0xFFB5B8BA.toInt()

    /**
     * The silver face of a home key. It lives here rather than in HomeActivity
     * because the key editor shows the same face in miniature, and a pressed key
     * is only a tint of it. The greys are shared by every brand; the rim stroke
     * is the accent, which is why a resource file cannot hold this.
     */
    fun keyFace(context: Context, palette: BrandPalette, pressed: Boolean = false): Drawable {
        val density = context.resources.displayMetrics.density
        val face = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            if (pressed) intArrayOf(palette.circlePressedTop, palette.circlePressedBottom)
            else intArrayOf(KEY_TOP, KEY_BOTTOM)
        ).apply {
            shape = GradientDrawable.OVAL
            val strokeDp = HomeScreenSpec.circleAccentStrokeDp + if (pressed) 1 else 0
            setStroke(
                (strokeDp * density).toInt().coerceAtLeast(1),
                if (pressed) palette.circlePressedBorder else palette.accent
            )
        }
        val edge = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(KEY_EDGE)
        }
        val rim = (HomeScreenSpec.circleDarkRimDp * density).toInt()
        return LayerDrawable(arrayOf(edge, InsetDrawable(face, rim)))
    }

    /** Frame drawn around the chosen icon in the key editor's grid. */
    fun selectedFrame(context: Context, palette: BrandPalette): Drawable {
        val density = context.resources.displayMetrics.density
        return rounded(
            palette.accentSoft,
            ROW_RADIUS_DP * density,
            (2 * density).toInt().coerceAtLeast(1),
            palette.accent
        )
    }

    private fun rounded(
        fill: Int,
        radius: Float,
        strokeWidth: Int = 0,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius
        setColor(fill)
        if (strokeWidth > 0) setStroke(strokeWidth, strokeColor)
    }
}

/** Runs [action] on this view and every descendant, depth first. */
internal fun View.forEachDescendant(action: (View) -> Unit) {
    action(this)
    if (this is ViewGroup) {
        for (index in 0 until childCount) getChildAt(index).forEachDescendant(action)
    }
}
