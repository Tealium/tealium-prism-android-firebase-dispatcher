import java.net.URI
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    `maven-publish`
}

group = "com.tealium.prism"
version = "0.1.0"

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

/**
 * Name of a template directory under the repository's `docs/` folder, or an empty string for the
 * stock Dokka look.
 *
 * Passing `-PDOKKA_TEMPLATES_DIR=docs-tealium-com` builds the docs.tealium.com variant, which is
 * published to the `gh-pages` branch and consumed by the Hugo site. The stock build keeps going to
 * GitHub Pages, so both variants can be produced from a single checkout without clobbering each other.
 */
val dokkaTemplatesDir: String =
    (project.findProperty("DOKKA_TEMPLATES_DIR") as String?)
        ?: System.getenv("DOKKA_TEMPLATES_DIR")
        ?: ""

extensions.configure(DokkaExtension::class.java) {
    moduleName.set("Tealium Prism Firebase Dispatcher")

    if (dokkaTemplatesDir.isNotBlank()) {
        dokkaPublications.named("html") {
            outputDirectory.set(layout.buildDirectory.dir("dokka/html-$dokkaTemplatesDir"))
        }
    }

    dokkaSourceSets.named("main") {
        includes.from("Module.md", "Packages.md")

        sourceLink {
            localDirectory.set(file("src/main/java"))
            remoteUrl("https://github.com/Tealium/tealium-prism-android-firebase-dispatcher/tree/main/firebase/src/main/java")
            remoteLineSuffix.set("#L")
        }

        // Filter out all `internal` classes from the generated documentation.
        perPackageOption {
            matchingRegex.set(".*internal.*")
            suppress.set(true)
        }
    }

    pluginsConfiguration.named("html", DokkaHtmlPluginParameters::class.java) {
        footerMessage.set("(c) Tealium 2026")

        if (dokkaTemplatesDir.isNotBlank()) {
            val templates = rootProject.file("docs/$dokkaTemplatesDir")
            if (templates.exists()) {
                templatesDir.set(templates)
                customStyleSheets.from(rootProject.fileTree(templates) { include("**/*.css") })
            } else {
                logger.warn("Dokka templates directory not found: $templates; using defaults")
            }
        }
    }
}
