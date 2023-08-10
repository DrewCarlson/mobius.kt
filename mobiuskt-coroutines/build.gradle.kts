plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    alias(libs.plugins.mavenPublish)
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
            testTask(Action {
                useKarma {
                    useFirefoxHeadless()
                }
            })
        }
    }
    val nativeTargets = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        tvosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64(),
        watchosDeviceArm64(),
        watchosX64(),
        macosX64(),
        macosArm64(),
        linuxX64(),
        linuxArm64(),
        mingwX64(),
    )
    configure(nativeTargets) {
        compilations.getByName("test") {
            defaultSourceSet {
                kotlin.srcDir("src/nativeTest/kotlin")
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
        named("commonMain") {
            dependencies {
                implementation(projects.mobiusktCore)
                implementation(libs.coroutines.core)
                implementation(libs.atomicfu)
            }
        }

        named("commonTest") {
            dependencies {
                implementation(projects.mobiusktTest)
                implementation(libs.coroutines.test)
                implementation(libs.turbine)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
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
