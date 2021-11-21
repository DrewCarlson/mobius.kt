plugins {
    kotlin("multiplatform")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    ios()
    watchos()
    tvos()
    iosSimulatorArm64()
    tvosSimulatorArm64()
    watchosSimulatorArm64()
    macosArm64()
    macosX64("macos")
    linuxX64("linux")
    mingwX64("windows")
    jvm()
    js(BOTH) {
        nodejs()
        browser()
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        named("commonMain") {
            dependencies {
                implementation(project(":mobiuskt-core"))
                implementation(project(":mobiuskt-internal"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$COROUTINES_VERSION")
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
