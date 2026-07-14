import java.net.URI

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

group = "com.tealium.prism"
version = "1.0.0"

// Suffix with -SNAPSHOT when publishing a snapshot build (e.g. -PSNAPSHOT=true).
val resolvedVersion: String = run {
    val isSnapshot = (findProperty("SNAPSHOT") as String?).toBoolean()
    if (isSnapshot && !version.toString().endsWith("-SNAPSHOT")) "$version-SNAPSHOT" else version.toString()
}

android {
    namespace = "com.tealium.prism.firebase"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        buildConfigField("String", "VERSION", "\"$resolvedVersion\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    testOptions {
        targetSdk = 35
        unitTests.isIncludeAndroidResources = true
    }

    publishing {
        singleVariant("release")
    }
}

// Reads a value from a Gradle property first, then the environment. Used for AWS
// credentials so CI can supply them via environment variables.
fun propertyOrEnv(name: String): String? = findProperty(name) as String? ?: System.getenv(name)

publishing {
    publications {
        create<MavenPublication>("Release") {
            groupId = group.toString()
            artifactId = "firebase"
            version = resolvedVersion

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "Release"
            url = URI("s3://maven.tealiumiq.com/android/releases/")
            credentials(AwsCredentials::class.java) {
                accessKey = propertyOrEnv("AWS_ACCESS_KEY")
                secretKey = propertyOrEnv("AWS_SECRET_KEY")
                sessionToken = propertyOrEnv("AWS_SESSION_TOKEN")
            }
        }
        maven {
            name = "Snapshot"
            url = URI("s3://maven.tealiumiq.com/android/snapshots/")
            credentials(AwsCredentials::class.java) {
                accessKey = propertyOrEnv("AWS_ACCESS_KEY")
                secretKey = propertyOrEnv("AWS_SECRET_KEY")
                sessionToken = propertyOrEnv("AWS_SESSION_TOKEN")
            }
        }
    }
}

dependencies {
    api(platform(libs.prism.bom))
    api(libs.prism.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
}
