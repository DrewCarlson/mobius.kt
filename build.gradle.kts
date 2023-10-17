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
        classpath(libs.atomicfu.plugin) {
            exclude("org.jetbrains.kotlin", "kotlin-gradle-plugin-api")
        }
    }
}

plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompat) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.composeMultiplatform) apply false
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

extensions.configure<kotlinx.kover.api.KoverMergedConfig> {
    enable()
    filters {
        projects {
            excludes.add(":mobiuskt-test")
            excludes.add(":mobiuskt-codegen")
            excludes.add(":mobiuskt-codegen-api")
            excludes.add(":mobiuskt-codegen-test")
        }
    }
}
