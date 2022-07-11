plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("com.android.library")
}

apply(plugin = "kotlinx-atomicfu")
apply(from = "../gradle/publishing.gradle.kts")

android {
    compileSdk = 31
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

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }
    jvm()
    js(IR) {
        binaries.library()
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
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
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

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(libs.guava)
                implementation(libs.awaitility)
                implementation(libs.slf4j)
            }
        }

        val androidMain by getting {
            dependsOn(jvmMain)
            dependencies {
                implementation(libs.androidx.livedata)
                implementation(libs.androidx.viewmodel)
            }
        }

        val androidTest by getting {
            dependsOn(jvmTest)
            dependencies {
                implementation(projects.mobiusktTest)
                implementation(libs.androidx.lifecycleRuntime)
                implementation(libs.androidx.coreTesting)
                implementation(libs.androidx.test.runner)
                implementation(libs.robolectric)
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
