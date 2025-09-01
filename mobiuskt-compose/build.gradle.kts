@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
}

android {
    compileSdk = 35
    namespace = "kt.mobius.compose"
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
    buildFeatures {
        compose = true
    }
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }
    jvmToolchain(17)

    androidTarget()
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    js(IR) {
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
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        //tvosX64(),
        //tvosArm64(),
        //tvosSimulatorArm64(),
        //watchosArm32(),
        //watchosArm64(),
        //watchosSimulatorArm64(),
        //watchosDeviceArm64(),
        //watchosX64(),
        macosX64(),
        macosArm64(),
        //linuxX64(),
        //linuxArm64(),
        //mingwX64(),
    )
    sourceSets {
        all {
            explicitApi()
            languageSettings {
                optIn("kt.mobius.compose.ExperimentalMobiusktComposeApi")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(projects.mobiusktCore)
                implementation(projects.mobiusktExtras)
                implementation(libs.coroutines.core)
                implementation(compose.runtime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.coroutines.test)
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.livedata)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
            }
        }

        val androidInstrumentedTest by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.ui)
                implementation(libs.androidx.compose.runtime.android)
                implementation(libs.androidx.compose.foundation)
                implementation(libs.androidx.compose.material)
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.compose.ui.test.manifest)
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.uiTestJUnit4)
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(compose.html.testUtils)
                implementation(kotlin("test"))
                implementation(kotlin("test-js"))
            }
        }
    }
}
