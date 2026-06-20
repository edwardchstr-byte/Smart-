plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.companion.aria"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.companion.aria"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Override with -PbackendUrl=https://your-server.example.com when building,
        // or change the default below to point at your deployed backend.
        buildConfigField("String", "BACKEND_URL", "\"${project.findProperty("backendUrl") ?: "http://10.0.2.2:3000"}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
}
