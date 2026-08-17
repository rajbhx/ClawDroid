import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.clawdroid.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.clawdroid.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        ndk { abiFilters += listOf("arm64-v8a") }

        buildConfigField(
            "String",
            "BOOTSTRAP_URL",
            "\"https://github.com/termux/termux-packages/releases/download/bootstrap-2026.08.16-r1%2Bapt.android-7/bootstrap-aarch64.zip\"",
        )
        buildConfigField(
            "String",
            "WWW_URL",
            "\"https://raw.githubusercontent.com/rajbhx/ClawDroid/main/android/app/src/main/assets/www.zip\"",
        )
        buildConfigField(
            "String",
            "CONFIG_URL",
            "\"https://raw.githubusercontent.com/rajbhx/ClawDroid/main/config.json\"",
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val props = project.rootProject.file("local.properties")
            if (props.exists()) {
                val localProps = Properties().apply { props.inputStream().use { load(it) } }
                storeFile = file(localProps.getProperty("RELEASE_STORE_FILE", ""))
                storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD", "")
                keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS", "")
                keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        jniLibs { useLegacyPackaging = true }
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // Terminal modules
    implementation(project(":terminal-emulator"))
    implementation(project(":terminal-view"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.runtime)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.markcompose)
    debugImplementation(libs.compose.ui.tooling)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room (memory database)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // OkHttp (provider routing + SSE)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.okhttp.logging)

    // ONNX Runtime (embeddings)
    implementation(libs.onnxruntime)

    // JSON
    implementation(libs.gson)
    implementation(libs.serialization.json)

    // DataStore (settings)
    implementation(libs.datastore.preferences)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
