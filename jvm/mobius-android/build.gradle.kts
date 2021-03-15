plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
}

apply(from = rootProject.file("gradle/publishing.gradle.kts"))

android {
    compileSdkVersion(28)
    defaultConfig {
        minSdkVersion(21)
    }
    lintOptions {
        disable("InvalidPackage")
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

kotlin {
    android()

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":mobius-core"))
            }
        }
    }
}
