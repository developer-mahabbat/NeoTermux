plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.neotermux.termlib"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-O3", "-DUNIX", "-DANDROID")
                abiFilters("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}