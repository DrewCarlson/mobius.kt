plugins {
    kotlin("multiplatform")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    ios()
    //watchos()
    tvos()
    macosX64("macos")
    linuxX64("linux")
    mingwX64("windows")
    jvm()
    js(BOTH) {
        nodejs()
        browser()
    }
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":mobiuskt-core"))
                implementation(project(":mobiuskt-internal"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
            }
        }

        named("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.3-native-mt")
            }
        }

        named("jsTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-js"))
            }
        }
    }
}
