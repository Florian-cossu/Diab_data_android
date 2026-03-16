import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "2.2.20-2.0.4"
    id("com.google.protobuf") version "0.9.6"
}

android {
    namespace = "com.diabdata"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.diabdata"
        minSdk = 26
        targetSdk = 36
        versionCode = getVersionCode()
        versionName = "4.8.0"
        buildConfigField("String", "MEDICATION_GTIN_FILE_VERSION", "\"1.2.0\"")
        buildConfigField("String", "MEDICAL_DEVICES_GTIN_FILE_VERSION", "\"1.0.3\"")

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

    sourceSets {
        getByName("main") {
            java.srcDir("build/generated/source/proto/main/java")
            assets.srcDir("src/main/proto")
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/MANIFEST.MF",
                "META-INF/io.netty.versions.properties"
            )
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
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

    // BOM
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text)

    // Image loading
    implementation(libs.coil.compose)

    // Vico
    implementation(libs.vico.compose)
    // implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    // implementation(libs.vico.multiplatform)
    // implementation(libs.vico.views)

    // Coroutines
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.androidx.navigation.compose)

    // Material - CHOOSE ONLY ONE
    // implementation(libs.androidx.material3)           // Stable version
    // Or expressive alpha version :
    implementation(libs.material3.expressive)      // Unstable expressive version
    implementation(libs.material)
    implementation(libs.androidx.material.icons.extended)

    // Adaptive compose libs
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)

    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.animation)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.work.runtime.ktx)

    // Widgets
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)
    implementation(libs.androidx.lifecycle.process)

    // Wear OS complication
    implementation(libs.androidx.watchface.complications.data.source)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)

    // Annotation processing
    ksp(libs.androidx.room.compiler)
    implementation(libs.gson)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ZXing
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // KTOR SERVER ON DEVICE
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)

    // Ktor + GSON
    implementation(libs.ktor.serialization.gson)

    // Shared
    implementation(project(":shared"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.27.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
