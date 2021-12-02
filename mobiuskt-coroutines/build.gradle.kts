plugins {
    kotlin("multiplatform")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    ios()
    watchos()
    tvos()
    jvm()
    js(BOTH) {
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
        tvosSimulatorArm64(),
        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64(),
        watchosX86(),
        watchosX64(),
        macosX64("macos"),
        macosArm64(),
        linuxX64("linux"),
        mingwX64("windows"),
        mingwX86("windowsX86"),
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
