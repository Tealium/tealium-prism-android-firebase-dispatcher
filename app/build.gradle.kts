plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.tealium.prism.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tealium.prism.example"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    // Firebase dispatcher module
    // LOCAL DEVELOPMENT: Uses local :firebase module from this project
    // When published to Maven, change to:
    //   implementation("com.tealium.prism:firebase:1.0.0")
    // Or use debug/release split like in tealium-android-firebase-remote-command:
    //   debugImplementation(project(":firebase"))
    //   releaseImplementation("com.tealium.prism:firebase:1.0.0")
    implementation(project(":firebase"))
    
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
}