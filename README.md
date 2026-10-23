# Golf 6 Launcher

Personal Android head-unit launcher for a Golf VI, with a dark factory-infotainment-inspired HOME screen. It is a private project and is not affiliated with Volkswagen.

The launcher only opens apps already installed on the radio; it does not implement tuner, CAN, Bluetooth, or Android Auto integration.

## Home screen

- Six circular, raised grey shortcut keys arranged in three mirrored pairs around a large, auto-fitting central clock, date and GPS speed, on a dark textured field with Volkswagen-red rings. A small settings gear sits in the upper-right corner; there is no duplicate header or footer bar.
- The complete interface is available in Czech, including a manual Czech option in language settings.
- Settings share the graphite-and-VW-red look and only expose controls wired to this home screen: language, app assignments for the five app-backed tiles, GPS access, keep-screen-on, default launcher, and hidden drawer apps.
- The center shows GPS speed as a centered number with `km/h` below it; without a fresh fix it shows `--`. Foreground location is used only while the home screen is visible.
- The app drawer sees only launchable apps; the install manifest does not declare notification-listener access.
- Radio uses a classic FM set with an antenna; navigation uses a map-marker icon to distinguish it from App-Connect. The tiles launch the chosen installed app; App-Connect displays a gray-and-white version of Google's Android Auto launcher mark.
- If no app is configured for a tile, tapping it opens the app picker; choosing an app launches it.
- Long-press an app-backed tile to change its assigned app.
- Apps opens the app drawer. The gear opens launcher settings.
- Czech labels are used when Android's language is Czech; otherwise English labels are shown.

The visual direction follows a red-accent RNS reference. Design-reference images are kept locally under `downloaded_images/` and intentionally excluded from Git because their usage rights are separate from the app source. The App-Connect shortcut uses Google's Android Auto launcher mark; no VW/Škoda logos or screenshots are embedded.

## Build

Prerequisites: JDK 17 and Android SDK Platform 36 + Build-Tools 36.0.0. Set `ANDROID_HOME` if the SDK is not at `~/Android/Sdk`.

```bash
./gradlew testLegacyDebugUnitTest assembleLegacyDebug
```

Debug APK:

```text
app/build/outputs/apk/legacy/debug/app-legacy-debug.apk
```

The APK is not yet tested on the target head unit. Install it, select **Golf 6 Launcher** as the HOME app, and verify the screen ratio, assigned apps and boot behavior. Keep the stock launcher available so it can be selected again if needed.

## Source and license

This project started from [Dashline](https://github.com/metehankaygsz/dashline), licensed under GPL-3.0-only; its license and upstream README are retained in this directory. The custom Golf 6 HOME screen is a small local modification. No distribution is intended.
