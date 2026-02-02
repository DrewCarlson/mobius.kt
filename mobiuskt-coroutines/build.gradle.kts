@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.atomicfu)
    alias(libs.plugins.kover)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }
    jvmToolchain(17)
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    js(IR) {
        binaries.executable()
        nodejs()
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()
    watchosX64()
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    sourceSets {
        all {
            explicitApi()
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
                if (name.endsWith("Test")) {
                    optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                }
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(projects.mobiusktCore)
                implementation(libs.coroutines.core)
                implementation(libs.atomicfu)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.mobiusktTest)
                implementation(libs.coroutines.test)
                implementation(libs.turbine)
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
