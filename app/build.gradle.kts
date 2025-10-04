import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

android {
    namespace = "com.diabdata"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.diabdata"
        minSdk = 26
        targetSdk = 36
        versionCode = getVersionCode()
        versionName = "3.9.9"
        buildConfigField("String", "MEDICATION_GTIN_FILE_VERSION", "\"1.2.0\"")
        buildConfigField("String", "MEDICAL_DEVICES_GTIN_FILE_VERSION", "\"1.0.2\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildToolsVersion = "36.0.0"
}

fun getVersionCode(): Int {
    val versionFile = file("version.properties")
    if (!versionFile.exists()) {
        versionFile.writeText("1")
    }
    val current = versionFile.readText().trim().replace(
        regex = Regex("=$"),
        replacement = ""
    ).toInt()
    val newCode = current + 1
    versionFile.writeText(newCode.toString())
    return newCode
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.multiplatform)
    implementation(libs.vico.views)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.navigation.animation)

    // Material
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material3)

    // Foundation
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.animation.core)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.androidx.foundation.layout)

    //Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.okhttp)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)


    // Use ksp for annotation processing
    ksp(libs.androidx.room.compiler)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)


    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ... scanner dependencies ...
    implementation(libs.barcode.scanning)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ... ZXing dependencies ...
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}