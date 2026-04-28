plugins {
    // Explicitly apply the Android plugin here
    id("com.android.application") version "9.2.0"
    id("org.jetbrains.kotlin.plugin.compose")
    //id("org.jetbrains.kotlin.android") version "2.3.21"
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.stremflix.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.stremflix.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.1")
}