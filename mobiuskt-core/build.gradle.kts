plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apply(plugin = "kotlinx-atomicfu")
apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    jvm()
    js(IR) {
        nodejs()
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }

    ios()
    watchos()
    tvos()

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
            //mingwX86("windowsX86"),
    )
    val darwinTargets = listOf("ios", "tvos", "watchos", "macos")
    configure(nativeTargets) {
        compilations.getByName("main") {
            defaultSourceSet {
                kotlin.srcDir("src/nativeMain/kotlin")

                if (darwinTargets.any(this@configure.name::startsWith)) {
                    kotlin.srcDir("src/darwinMain/kotlin")
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        all {
            explicitApi()
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$ATOMICFU_VERSION")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("com.google.guava:guava:$GUAVA_VERSION")
                implementation("org.awaitility:awaitility:$AWAITALITY_VERSION")
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
