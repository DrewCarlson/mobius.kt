plugins {
    kotlin("multiplatform")
}

val guava_version: String by ext
val awaitality_version: String by ext

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    jvm()
    js(BOTH) {
        nodejs()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
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
        tvosX64(),
        tvosArm64(),
        watchosArm32(),
        watchosArm64(),
        watchosX86(),
        watchosX64(),
        macosX64("macos"),
        linuxX64("linux"),
        mingwX64("windows")
    )
    configure(nativeTargets) {
        compilations.getByName("main") {
            defaultSourceSet {
                kotlin.srcDir("src/nativeMain/kotlin")
            }
        }
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":mobiuskt-internal"))
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
                implementation("com.google.guava:guava:$guava_version")
                implementation("org.awaitility:awaitility:$awaitality_version")
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
