plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    ios()
    watchos()
    tvos()
    iosSimulatorArm64()
    tvosSimulatorArm64()
    watchosSimulatorArm64()
    watchosX86()
    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()
    //mingwX86()
    jvm()
    js(IR) {
        nodejs()
        browser()
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        all {
            explicitApi()
        }
    }
}
