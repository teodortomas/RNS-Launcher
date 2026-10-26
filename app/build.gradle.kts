import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Signing credentials come from keystore.properties (gitignored) or, in CI, from
// environment variables. Never commit either. See README → Building a release.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun signingValue(key: String, env: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(env)

val releaseStoreFile = signingValue("storeFile", "KEYSTORE_FILE")
val releaseStorePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "KEY_PASSWORD")
val hasReleaseSigning = releaseStoreFile != null && releaseStorePassword != null &&
    releaseKeyAlias != null && releaseKeyPassword != null

// Release builds get their version from the git tag (see .github/workflows/release.yml);
// local builds fall back to these defaults.
val appVersionName = (project.findProperty("appVersionName") as String?) ?: "0.6.0"
val appVersionCode = (project.findProperty("appVersionCode") as String?)?.toIntOrNull() ?: 600

android {
    namespace = "eu.teodortomas.rnslauncher"
    // Google Play requires targeting API 35 now and API 36 from 31 Aug 2026.
    compileSdk = 36

    defaultConfig {
        applicationId = "eu.teodortomas.rnslauncher"
        // Android 4.4 support is retired; all published builds require Android 5.0+.
        minSdk = 21
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        // Let the support library render vector drawables on pre-Lollipop devices.
        vectorDrawables.useSupportLibrary = true
    }

    // Keep one Android 5.0+ distribution variant. The former KitKat sideload
    // flavour and its self-updater are retired; GitHub Releases now use this APK.
    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
            buildConfigField("boolean", "UPDATE_CHECK", "false")
        }
    }

    signingConfigs {
        // Only registered when credentials are actually available, so cloning the
        // repo and running assembleDebug works with no setup at all.
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Unsigned release builds still succeed; they just can't be installed.
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        // No applicationIdSuffix on debug: this app registers as HOME, and a
        // separate package would show up as a second launcher and lose settings.
    }

    lint {
        // A broken build should fail CI; style warnings shouldn't.
        abortOnError = false
        checkReleaseBuilds = false
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Keep deps minimal — many head units lack Play Services and are slow.
    // The app uses the platform LocationManager and does not require Play Services.
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Pulled in by appcompat anyway; declared so the runtime icon tinting can
    // reference VectorDrawableCompat directly. 1.1.0 still supports minSdk 14.
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    testImplementation("junit:junit:4.13.2")
    // Weather uses the keyless Open-Meteo HTTP API + plain LocationManager, so no
    // Google Play Services / Maps SDK is needed — keeps the widest device support.
}
