pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "tealium-prism-android-firebase-dispatcher"

// ============================================================================
// LOCAL DEVELOPMENT SETUP (current)
// ============================================================================
// Uses local tealium-kotlin-v2 project via composite build
// This allows using the local :core module instead of downloading from Maven
includeBuild("../tealium-kotlin-v2") {
    dependencySubstitution {
        // Maps Maven dependency to local project module
        substitute(module("com.tealium.prism:prism-core")).using(project(":core"))
    }
}

// ============================================================================
// PRODUCTION SETUP (when Prism SDK is published to Maven)
// ============================================================================
// Remove the includeBuild block above and uncomment the following:
//
// dependencyResolutionManagement {
//     repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//     repositories {
//         google()
//         mavenCentral()
//         maven {
//             url = uri("https://maven.tealiumiq.com/android/releases/")
//         }
//     }
// }
//
// The dependency in firebase/build.gradle.kts remains the same:
//   api("com.tealium.prism:prism-core")
// It will automatically resolve from Maven instead of local project.

include(":app")
include(":firebase")
