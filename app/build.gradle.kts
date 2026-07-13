plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.neotermux.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.neotermux.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86") }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("Boolean", "DEBUG_MODE", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("Boolean", "DEBUG_MODE", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/AL2.0"
            excludes += "/META-INF/LGPL2.1"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/OSGI-INF/**"
            excludes += "/META-INF/versions/9/OSGI-INF/**"
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true
        }
    }
}

dependencies {
    implementation(project(":termlib"))
    implementation(project(":editor"))
    implementation(project(":filemanager"))
    implementation(project(":sshclient"))
    implementation(project(":gitlib"))
    implementation(project(":terminal-emulator"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.animation)
    implementation(libs.compose.runtime)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.service)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)

    implementation(libs.datastore)

    implementation(libs.security.crypto)

    implementation(libs.coil)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}