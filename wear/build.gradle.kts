plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.diabdata.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.diabdata"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.7"

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Platform BOMs for consistent versions
    implementation(platform(libs.androidx.compose.bom))

    // Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    // Wear specific
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.horologist.compose.tools)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.wear)

    // Tiles - These are the key changes
    implementation(libs.androidx.tiles.material) // The material components for tiles (now using the single, correct alias)
    implementation(libs.horologist.tiles)

    // Protolayout (correctly declared)
    implementation(libs.androidx.protolayout)
    implementation(libs.androidx.protolayout.material3)
    implementation(libs.androidx.protolayout.expression)

    // Watch Face & Complications
    implementation(libs.androidx.watchface)
    implementation(libs.androidx.watchface.complications.data.source)
    implementation(libs.androidx.watchface.complications.data.source.ktx)

    // Data Layer & Coroutines
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)

    // Other utilities
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(project(":shared"))
    implementation(libs.core.ktx)

    // Debug and Test Implementations
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.renderer)
    debugImplementation(libs.tiles.tooling.preview)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.tiles.testing)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.runner)
}