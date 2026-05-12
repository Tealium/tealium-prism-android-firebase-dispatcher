plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    maven(url = "https://maven.google.com/")
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(libs.eclipse.jgit)
}

gradlePlugin {
    plugins {
        register("tealiumPlugin") {
            id = "tealium-plugin"
            implementationClass = "com.tealium.gradle.TealiumGradlePlugin"
        }
    }
}
