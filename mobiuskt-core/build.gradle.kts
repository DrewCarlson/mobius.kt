plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("com.android.library")
}

apply(plugin = "kotlinx-atomicfu")
apply(from = "../gradle/publishing.gradle.kts")

android {
    compileSdk = 28
    defaultConfig {
        minSdk = 21
    }
    lint {
        disable("InvalidPackage")
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

kotlin {
    android()
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
            macosX64(),
            macosArm64(),
            linuxX64(),
            mingwX64(),
            //mingwX86(),
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
                implementation(libs.atomicfu)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.mobiusktTest)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmCommonMain by creating {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependsOn(jvmCommonMain)
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(libs.guava)
                implementation(libs.awaitility)
                implementation(libs.slf4j)
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
