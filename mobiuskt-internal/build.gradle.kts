plugins {
    kotlin("multiplatform")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    ios()
    watchos()
    tvos()
    macosX64("macos")
    linuxX64("linux")
    mingwX64("windows")
    jvm()
    js(BOTH) {
        nodejs()
        browser()
    }

    val nativeTargets = listOf(
        iosX64(),
        iosArm64(),
        tvosX64(),
        tvosArm64(),
        watchosArm32(),
        watchosArm64(),
        watchosX86(),
        watchosX64(),
        macosX64("macos"),
        linuxX64("linux"),
        mingwX64("windows")
    )
    configure(nativeTargets) {
        compilations.getByName("main") {
            defaultSourceSet {
                kotlin.srcDir("src/nativeMain/kotlin")
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-js"))
            }
        }
    }
}
