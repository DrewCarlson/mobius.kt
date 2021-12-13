plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("com.android.library")
}

apply(from = rootProject.file("gradle/publishing.gradle.kts"))

android {
    compileSdk = 28
    defaultConfig {
        minSdk = 21
    }
    lint {
        disable("InvalidPackage")
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

kotlin {
    android()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        all {
            explicitApi()
        }

        val androidMain by getting {
            dependencies {
                implementation(projects.mobiusktCore)
            }
        }
    }
}
