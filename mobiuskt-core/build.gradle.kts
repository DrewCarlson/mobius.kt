plugins {
    kotlin("multiplatform")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            if (properties.containsKey("newmm")) {
                binaryOptions["memoryModel"] = "experimental"
            }
        }
    }
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
            mingwX86("windowsX86"),
    )
    configure(nativeTargets) {
        compilations.getByName("main") {
            defaultSourceSet {
                kotlin.srcDir("src/nativeMain/kotlin")
            }
        }
    }

    sourceSets {
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
