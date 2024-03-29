plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
}

apply(plugin = "kotlinx-atomicfu")

kotlin {
    jvm {
        jvmToolchain(11)
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
