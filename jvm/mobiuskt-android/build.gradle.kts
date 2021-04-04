plugins {
    kotlin("multiplatform")
    id("com.android.library")
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
