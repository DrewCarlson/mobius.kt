plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    alias(libs.plugins.mavenPublish)
}

kotlin {
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
    mingwX64()
    jvm {
        jvmToolchain(11)
    }
    js(IR) {
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
        }

        val commonMain by getting {
            dependencies {
                implementation(projects.mobiusktCore)
                implementation(libs.atomicfu)
                implementation(libs.immutableCollections)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
