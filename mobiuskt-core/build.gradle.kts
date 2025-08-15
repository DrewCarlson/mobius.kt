@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.atomicfu)
}

android {
    compileSdk = 35
    namespace = "kt.mobius.android"
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    lint {
        disable.add("InvalidPackage")
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
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

    sourceSets {
        all {
            explicitApi()
            languageSettings {
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlin.native.concurrent.ObsoleteWorkersApi")
            }
        }

        val commonMain by getting {
            dependencies {
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

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(libs.guava)
                implementation(libs.awaitility)
                implementation(libs.slf4j)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.livedata)
                implementation(libs.androidx.viewmodel)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(projects.mobiusktTest)
                implementation(libs.androidx.lifecycleRuntime)
                implementation(libs.androidx.coreTesting)
                implementation(libs.androidx.test.runner)
                implementation(libs.robolectric)
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
