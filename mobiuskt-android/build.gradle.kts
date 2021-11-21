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
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

kotlin {
    android {
        publishAllLibraryVariants()
    }

    sourceSets {
        val androidMain by getting {
            kotlin.srcDir("src/main/kotlin")
            dependencies {
                implementation(project(":mobiuskt-core"))
            }
        }
    }
}
