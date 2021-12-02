plugins {
    kotlin("multiplatform")
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

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":mobiuskt-core"))
            }
        }
    }
}
