@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
}

kotlin {
    jvmToolchain(11)
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()
    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()
    mingwX64()
    jvm()
    js(IR) {
        binaries.library()
        nodejs()
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    wasmWasi {
        nodejs()
    }

    sourceSets {
        all {
            explicitApi()
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }

        val commonMain by getting {
            dependencies {
                implementation(projects.mobiusktCore)
                implementation(libs.atomicfu)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.mobiusktTest)
                implementation(kotlin("test"))
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
