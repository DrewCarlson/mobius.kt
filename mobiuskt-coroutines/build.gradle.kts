plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    ios()
    watchos()
    tvos()
    jvm()
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
    val nativeTargets = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        tvosX64(),
        tvosArm64(),
        // tvosSimulatorArm64(),
        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64(),
        // watchosX86(),
        watchosX64(),
        macosX64(),
        macosArm64(),
        linuxX64(),
        mingwX64(),
        // mingwX86(),
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
            }
        }

        named("commonTest") {
            dependencies {
                implementation(projects.mobiusktTest)
                implementation(libs.atomicfu)
                implementation(libs.coroutines.test)
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
