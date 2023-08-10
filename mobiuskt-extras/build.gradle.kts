plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    alias(libs.plugins.mavenPublish)
}

kotlin {
    ios()
    watchos()
    tvos()
    iosSimulatorArm64()
    tvosSimulatorArm64()
    watchosSimulatorArm64()
    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()
    jvm {
        jvmToolchain(11)
    }
    js(IR) {
        binaries.library()
        nodejs()
        browser {
            testTask(Action {
                useKarma {
                    useFirefoxHeadless()
                }
            })
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        all {
            explicitApi()
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }

        val commonMain by getting {
            dependencies {
                implementation(projects.mobiusktCore)
                implementation(libs.atomicfu)
                implementation(libs.immutableCollections)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.mobiusktTest)
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
