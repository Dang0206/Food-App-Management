plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.finalproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.finalproject"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation(libs.androidx.material.icons.extended)
    implementation("io.coil-kt:coil-compose:2.5.0")
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    // Google AI client for Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.1.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation(libs.hilt.android.v248)
    kapt(libs.hilt.compiler.v248)
    implementation(libs.androidx.hilt.navigation.compose.v120)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose.v270)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Room
    implementation(libs.androidx.room.runtime.v261)
    kapt(libs.androidx.room.compiler.v261)
    implementation(libs.androidx.room.ktx.v261)


}