plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.composeMultiplatform)
}

android {
    compileSdk = 33
    namespace = "kt.mobius.compose"
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    androidTarget()
    jvm {
        jvmToolchain(11)
    }
    js(IR) {
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
        //tvosX64(),
        //tvosArm64(),
        //tvosSimulatorArm64(),
        //watchosArm32(),
        //watchosArm64(),
        //watchosSimulatorArm64(),
        //watchosDeviceArm64(),
        //watchosX64(),
        macosX64(),
        macosArm64(),
        //linuxX64(),
        //linuxArm64(),
        //mingwX64(),
    )
    sourceSets {
        all {
            explicitApi()
        }
        named("commonMain") {
            dependencies {
                implementation(projects.mobiusktCore)
                implementation(projects.mobiusktExtras)
                implementation(libs.coroutines.core)
                implementation(compose.runtime)
            }
        }

        named("commonTest") {
            dependencies {
                implementation(libs.coroutines.test)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        named("jvmTest") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.uiTestJUnit4)
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        named("jsMain") {
            dependencies {
                implementation(compose.html.core)
            }
        }

        named("jsTest") {
            dependencies {
                implementation(compose.html.testUtils)
                implementation(kotlin("test"))
                implementation(kotlin("test-js"))
            }
        }
    }
}
