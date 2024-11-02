import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    dependencies {
        classpath(libs.agp)
    }
}

plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompat) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.atomicfu) apply false
    //id("com.louiscad.complete-kotlin") version "1.1.0"
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    plugins.withType<NodeJsRootPlugin> {
        the<YarnRootExtension>().lockFileDirectory = rootDir.resolve("gradle/kotlin-js-store")
    }
}


subprojects {
    System.getenv("GITHUB_REF_NAME")
        ?.takeIf { it.startsWith("v") }
        ?.let { version = it.removePrefix("v") }
    apply(plugin = "org.jetbrains.kotlinx.kover")
    kover {}
}

dependencies {
    kover(project(":mobiuskt-core"))
    kover(project(":mobiuskt-compose"))
    kover(project(":mobiuskt-coroutines"))
    kover(project(":mobiuskt-extras"))
}
