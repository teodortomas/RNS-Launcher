package eu.teodortomas.rnslauncher

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

/** Circular Golf VI launcher menu inspired by the supplied RNS screen reference. */
class HomeActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyTo(newBase))
    }

    private lateinit var prefs: Prefs
    private lateinit var clockView: TextView
    private lateinit var dateView: TextView
    private lateinit var speedValueView: TextView
    private lateinit var speedUnitView: TextView
    private var locationManager: LocationManager? = null
    private var lastGpsLocation: Location? = null
    private var launchAfterPick = false

    /** Brand skin selected in Settings; the palette is baked into the views. */
    private lateinit var brand: BrandTheme
    private lateinit var palette: BrandPalette

    private val handler = Handler(Looper.getMainLooper())
    private val clockTicker = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, CLOCK_REFRESH_MS)
        }
    }
    private val gpsFreshnessTicker = object : Runnable {
        override fun run() {
            if (lastGpsLocation?.let(::isFreshGpsFix) != true) showNoGpsSpeed()
            handler.postDelayed(this, GPS_FRESHNESS_REFRESH_MS)
        }
    }
    private val gpsLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastGpsLocation = location
            showGpsSpeed(location)
        }

        override fun onProviderDisabled(provider: String) {
            lastGpsLocation = null
            showNoGpsSpeed()
        }

        override fun onProviderEnabled(provider: String) = Unit

        @Deprecated("Deprecated by Android; retained for API 19 compatibility")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        brand = BrandTheme.current(this)
        palette = brand.palette
        setContentView(createHomeScreen())
        hideSystemBars()
        updateClock()
    }

    override fun onResume() {
        super.onResume()
        // Settings can switch the brand while this activity is paused, and every
        // accent is baked into the views at creation, so rebuild them.
        if (BrandTheme.current(this) != brand) {
            recreate()
            return
        }
        hideSystemBars()
        applyKeepScreenOn()
        updateClock()
        startGpsSpeedUpdates()
        handler.removeCallbacks(clockTicker)
        handler.removeCallbacks(gpsFreshnessTicker)
        handler.postDelayed(clockTicker, CLOCK_REFRESH_MS)
        handler.postDelayed(gpsFreshnessTicker, GPS_FRESHNESS_REFRESH_MS)
    }

    override fun onPause() {
        handler.removeCallbacks(clockTicker)
        handler.removeCallbacks(gpsFreshnessTicker)
        stopGpsSpeedUpdates()
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    private fun applyKeepScreenOn() {
        if (prefs.keepScreenOn) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) startGpsSpeedUpdates()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    @Deprecated("Legacy activity result is used to support Android 4.4 head units")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_APP_PICK || resultCode != Activity.RESULT_OK) return
        if (!launchAfterPick) return
        val packageName = data?.getStringExtra(AppPickerActivity.EXTRA_PACKAGE) ?: return
        if (!AppRepository.launch(this, packageName)) {
            Toast.makeText(this, getString(R.string.home_launch_failed, packageName), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createHomeScreen(): View {
        val root = FrameLayout(this).apply {
            setBackgroundColor(COLOR_BACKGROUND)
        }
        val menuSurface = RadialMenuSurface(this)
        HomeScreenSpec.buttons.forEach { position ->
            val tile = position.tile
            menuSurface.addMenuTile(
                button = createCircleButton(tile),
                caption = createCaption(tile),
                position = position
            )
        }
        menuSurface.setCenterLabel(createCenterLabel())
        root.addView(
            menuSurface,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

        val settingsGear = ImageView(this).apply {
            setImageResource(R.drawable.ic_settings)
            setColorFilter(palette.accent)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = settingsGearBackground()
            isClickable = true
            isFocusable = true
            contentDescription = getString(R.string.home_settings)
            ViewCompat.setElevation(this, dp(2).toFloat())
            setOnClickListener { startActivity(Intent(this@HomeActivity, SettingsActivity::class.java)) }
        }
        root.addView(
            settingsGear,
            FrameLayout.LayoutParams(dp(48), dp(48), Gravity.TOP or Gravity.END).apply {
                topMargin = dp(8)
                marginEnd = dp(12)
            }
        )
        return root
    }

    private fun createCircleButton(tile: HomeMenuTile): View {
        val button = FrameLayout(this).apply {
            background = circleBackgroundSelector()
            ViewCompat.setElevation(this, dp(3).toFloat())
            isClickable = true
            isFocusable = true
        }
        val icon = ImageView(this).apply {
            setImageResource(tile.iconRes)
            if (tile == HomeMenuTile.APP_CONNECT || tile == HomeMenuTile.NAVIGATION || tile == HomeMenuTile.RADIO) {
                clearColorFilter()
            } else {
                setColorFilter(COLOR_ICON)
            }
            contentDescription = null
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        button.addView(icon, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        ))

        val selectedApp = tile.appRole?.let { role ->
            prefs.candidatesFor(role).firstOrNull { AppRepository.isInstalled(this, it) }
        }
        val appLabel = selectedApp?.let { AppRepository.labelFor(this, it) }
            ?: if (tile.appRole == null) getString(R.string.all_apps) else getString(R.string.home_unassigned)
        button.contentDescription = "${getString(tile.titleRes)}. $appLabel"
        button.setOnClickListener { openTile(tile) }
        tile.appRole?.let { role ->
            button.setOnLongClickListener {
                openAppPicker(role, launchChosenApp = false)
                true
            }
        }
        return button
    }

    private fun createCaption(tile: HomeMenuTile): TextView =
        text(tile.titleRes, HomeScreenSpec.captionSizeSp, COLOR_PRIMARY_TEXT, bold = true).apply {
            gravity = Gravity.CENTER
            setLetterSpacingCompat(0.035f)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

    private fun createCenterLabel(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        clockView = text("00:00", HomeScreenSpec.centerClockSizeSp, COLOR_PRIMARY_TEXT).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                HomeScreenSpec.centerClockMinSizeSp.roundToInt(),
                HomeScreenSpec.centerClockSizeSp.roundToInt(),
                2,
                TypedValue.COMPLEX_UNIT_SP
            )
        }
        dateView = text("", HomeScreenSpec.centerDateSizeSp, COLOR_MUTED).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            includeFontPadding = false
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                HomeScreenSpec.centerDateMinSizeSp.roundToInt(),
                HomeScreenSpec.centerDateSizeSp.roundToInt(),
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
        }
        speedValueView = text(
            GpsSpeedFormatter.NO_FIX_VALUE,
            HomeScreenSpec.centerSpeedSizeSp,
            palette.accent,
            bold = true
        ).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            contentDescription = getString(R.string.home_gps_label)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                HomeScreenSpec.centerSpeedMinSizeSp.roundToInt(),
                HomeScreenSpec.centerSpeedSizeSp.roundToInt(),
                2,
                TypedValue.COMPLEX_UNIT_SP
            )
        }
        speedUnitView = text(GpsSpeedFormatter.UNIT, HomeScreenSpec.centerSpeedUnitSizeSp, palette.accent, bold = true).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
        }
        val fullWidthLine = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        addView(clockView, LinearLayout.LayoutParams(fullWidthLine))
        addView(dateView, LinearLayout.LayoutParams.MATCH_PARENT, dp(32))
        addView(speedValueView, LinearLayout.LayoutParams(fullWidthLine))
        addView(speedUnitView, LinearLayout.LayoutParams(fullWidthLine).apply { topMargin = dp(-4) })
    }

    private fun openTile(tile: HomeMenuTile) {
        val role = tile.appRole
        if (role == null) {
            startActivity(Intent(this, AppDrawerActivity::class.java))
            return
        }
        if (!AppRepository.launchFirstAvailable(this, prefs.candidatesFor(role))) {
            openAppPicker(role, launchChosenApp = true)
        }
    }

    private fun openAppPicker(role: String, launchChosenApp: Boolean) {
        launchAfterPick = launchChosenApp
        AppPickerActivity.start(this, role, REQUEST_APP_PICK)
    }

    private fun updateClock() {
        val now = Date()
        clockView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        dateView.text = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(now)
    }

    private fun startGpsSpeedUpdates() {
        if (!::speedValueView.isInitialized) return
        if (!Permissions.hasLocation(this)) {
            showNoGpsSpeed()
            if (!prefs.askedLocation) {
                prefs.askedLocation = true
                Permissions.requestLocation(this, REQUEST_LOCATION)
            }
            return
        }

        val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            showNoGpsSpeed()
            return
        }
        locationManager = manager
        try {
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lastGpsLocation = null
                showNoGpsSpeed()
                return
            }
            lastGpsLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastGpsLocation?.let(::showGpsSpeed) ?: showNoGpsSpeed()
            manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                GPS_UPDATE_INTERVAL_MS,
                0f,
                gpsLocationListener,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            showNoGpsSpeed()
        } catch (_: IllegalArgumentException) {
            showNoGpsSpeed()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopGpsSpeedUpdates() {
        locationManager?.let { manager ->
            val hasLocationPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            if (hasLocationPermission) {
                try {
                    manager.removeUpdates(gpsLocationListener)
                } catch (_: SecurityException) {
                    // The user may revoke location permission while the screen is open.
                }
            }
        }
        locationManager = null
        lastGpsLocation = null
        showNoGpsSpeed()
    }

    private fun showGpsSpeed(location: Location) {
        if (!isFreshGpsFix(location)) {
            showNoGpsSpeed()
            return
        }
        val speed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !location.hasSpeed()) {
            null
        } else {
            location.speed
        }
        speedValueView.text = GpsSpeedFormatter.valueFromMetersPerSecond(speed)
        speedValueView.contentDescription =
            "${getString(R.string.home_gps_label)} ${speedValueView.text} ${GpsSpeedFormatter.UNIT}"
    }

    private fun isFreshGpsFix(location: Location): Boolean {
        val ageMs = System.currentTimeMillis() - location.time
        return ageMs in 0..MAX_LOCATION_AGE_MS
    }

    private fun showNoGpsSpeed() {
        if (!::speedValueView.isInitialized) return
        speedValueView.text = GpsSpeedFormatter.NO_FIX_VALUE
        speedValueView.contentDescription =
            "${getString(R.string.home_gps_label)} ${GpsSpeedFormatter.NO_FIX_VALUE} ${GpsSpeedFormatter.UNIT}"
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun TextView.setLetterSpacingCompat(spacing: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) letterSpacing = spacing
    }

    private fun text(textRes: Int, size: Float, color: Int, bold: Boolean = false): TextView =
        text(getString(textRes), size, color, bold)

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            gravity = Gravity.CENTER_VERTICAL
        }

    private fun settingsGearBackground(): StateListDrawable = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_pressed), gearStateBackground(palette.accentSoft))
        addState(intArrayOf(android.R.attr.state_focused), gearStateBackground(palette.accentSoft))
        addState(intArrayOf(), gearStateBackground(0x00000000))
    }

    private fun gearStateBackground(color: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun circleBackgroundSelector(): StateListDrawable = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_pressed), circleBackground(pressed = true))
        addState(intArrayOf(android.R.attr.state_focused), circleBackground(pressed = true))
        addState(intArrayOf(), circleBackground(pressed = false))
    }

    private fun circleBackground(pressed: Boolean): Drawable {
        val outer = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(COLOR_CIRCLE_EDGE)
        }
        val face = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            if (pressed) intArrayOf(palette.circlePressedTop, palette.circlePressedBottom)
            else intArrayOf(COLOR_CIRCLE_TOP, COLOR_CIRCLE_BOTTOM)
        ).apply {
            shape = GradientDrawable.OVAL
            setStroke(
                dp(
                    if (pressed) HomeScreenSpec.circleAccentStrokeDp + 1
                    else HomeScreenSpec.circleAccentStrokeDp
                ),
                if (pressed) palette.circlePressedBorder else palette.accent
            )
        }
        return LayerDrawable(arrayOf(outer, InsetDrawable(face, dp(HomeScreenSpec.circleDarkRimDp))))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private data class MenuChild(
        val button: View,
        val caption: View,
        val position: RadialMenuPosition
    )

    private inner class RadialMenuSurface(context: Context) : FrameLayout(context) {
        private val menuChildren = mutableListOf<MenuChild>()
        private var centerLabel: View? = null
        private var buttonSize = 0
        private var captionWidth = 0
        private val captionHeight = dp(16)
        private val captionGap = dp(2)
        private val textureStep = dp(7).coerceAtLeast(1)
        private val texturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x0AFFFFFF
            strokeWidth = dp(0.5f)
        }
        private val secondaryTexturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x07000000
            strokeWidth = dp(0.5f)
        }
        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
        private val ringBounds = RectF()

        init {
            setWillNotDraw(false)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(COLOR_STAGE_TOP, COLOR_STAGE_BOTTOM)
            )
        }

        fun addMenuTile(button: View, caption: View, position: RadialMenuPosition) {
            menuChildren += MenuChild(button, caption, position)
            addView(button, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            addView(caption, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        }

        fun setCenterLabel(view: View) {
            centerLabel = view
            addView(view, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val measuredWidth = resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec)
            val measuredHeight = resolveSize(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec)
            setMeasuredDimension(measuredWidth, measuredHeight)

            val minHitTarget = dp(MIN_CIRCLE_DP)
            val maximumCircle = min(measuredWidth / 6, measuredHeight / 3)
            buttonSize = min(
                maximumCircle,
                maxOf(minHitTarget, min((measuredWidth * 0.12f).roundToInt(), (measuredHeight * 0.23f).roundToInt()))
            ).coerceAtLeast(1)
            captionWidth = min(dp(CAPTION_WIDTH_DP), (measuredWidth * 0.23f).roundToInt()).coerceAtLeast(1)

            val exactButton = MeasureSpec.makeMeasureSpec(buttonSize, MeasureSpec.EXACTLY)
            val exactCaptionWidth = MeasureSpec.makeMeasureSpec(captionWidth, MeasureSpec.EXACTLY)
            val exactCaptionHeight = MeasureSpec.makeMeasureSpec(captionHeight, MeasureSpec.EXACTLY)
            val logoInset = HomeScreenSpec.logoInsetForButton(buttonSize)
            menuChildren.forEach { child ->
                child.button.setPadding(logoInset, logoInset, logoInset, logoInset)
                child.button.measure(exactButton, exactButton)
                child.caption.measure(exactCaptionWidth, exactCaptionHeight)
            }
            centerLabel?.measure(
                MeasureSpec.makeMeasureSpec(
                    (measuredWidth * HomeScreenSpec.centerLabelWidthPermille / 1_000f).roundToInt(),
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(dp(HomeScreenSpec.centerLabelMaxHeightDp), MeasureSpec.AT_MOST)
            )
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val contentWidth = right - left
            val contentHeight = bottom - top
            menuChildren.forEach { child ->
                val centerX = contentWidth * child.position.xPermille / 1_000
                val centerY = contentHeight * child.position.yPermille / 1_000
                val buttonLeft = centerX - buttonSize / 2
                val buttonTop = centerY - buttonSize / 2
                child.button.layout(buttonLeft, buttonTop, buttonLeft + buttonSize, buttonTop + buttonSize)
                val captionLeft = centerX - captionWidth / 2
                val captionTop = centerY + buttonSize / 2 + captionGap
                child.caption.layout(
                    captionLeft,
                    captionTop,
                    captionLeft + captionWidth,
                    captionTop + captionHeight
                )
            }
            centerLabel?.let { label ->
                val labelLeft = (contentWidth - label.measuredWidth) / 2
                val labelTop = (contentHeight * CENTER_LABEL_Y).roundToInt() - label.measuredHeight / 2
                label.layout(labelLeft, labelTop, labelLeft + label.measuredWidth, labelTop + label.measuredHeight)
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val width = width.toFloat()
            val height = height.toFloat()
            for (offset in -height.toInt()..width.toInt() step textureStep) {
                canvas.drawLine(offset.toFloat(), 0f, offset + height, height, texturePaint)
            }
            for (offset in 0..(width + height).toInt() step textureStep) {
                canvas.drawLine(offset.toFloat(), 0f, offset - height, height, secondaryTexturePaint)
            }

            val radiusX = width * HomeScreenSpec.ellipseRadiusXPermille / 1_000f
            val radiusY = height * HomeScreenSpec.ellipseRadiusYPermille / 1_000f
            ringBounds.set(width / 2f - radiusX, height / 2f - radiusY, width / 2f + radiusX, height / 2f + radiusY)
            ringPaint.color = COLOR_RING_SHADOW
            ringPaint.strokeWidth = dp(3f)
            canvas.drawOval(ringBounds, ringPaint)
            ringPaint.color = palette.accent
            ringPaint.strokeWidth = dp(1.2f)
            canvas.drawOval(ringBounds, ringPaint)
        }
    }

    companion object {
        private const val REQUEST_APP_PICK = 312
        private const val REQUEST_LOCATION = 313
        private const val CLOCK_REFRESH_MS = 30_000L
        private const val GPS_FRESHNESS_REFRESH_MS = 5_000L
        private const val GPS_UPDATE_INTERVAL_MS = 1_000L
        private const val MAX_LOCATION_AGE_MS = 15_000L
        private const val MIN_CIRCLE_DP = 48
        private const val CAPTION_WIDTH_DP = 112
        private const val CENTER_LABEL_Y = 0.53f

        private const val COLOR_BACKGROUND = 0xFF090A0B.toInt()
        private const val COLOR_STAGE_TOP = 0xFF181A1C.toInt()
        private const val COLOR_STAGE_BOTTOM = 0xFF0B0C0D.toInt()
        private const val COLOR_PRIMARY_TEXT = 0xFFECEEEF.toInt()
        private const val COLOR_MUTED = 0xFF9CA1A4.toInt()
        private const val COLOR_ICON = 0xFF424649.toInt()
        private const val COLOR_CIRCLE_EDGE = 0xFF555A5D.toInt()
        private const val COLOR_CIRCLE_TOP = 0xFFE0E2E3.toInt()
        private const val COLOR_CIRCLE_BOTTOM = 0xFFB5B8BA.toInt()
        private const val COLOR_RING_SHADOW = 0x705A5D60
    }
}
