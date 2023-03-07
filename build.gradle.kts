import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

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

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompat) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish) apply false
    id("com.louiscad.complete-kotlin") version "1.1.0"
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
    apply(plugin = "org.jetbrains.kotlinx.kover")
    kover {}
}

extensions.configure<kotlinx.kover.api.KoverMergedConfig> {
    enable()
    filters {
        projects {
            excludes.add(":mobiuskt-test")
        }
    }
}

// Required for doc publishing
System.getenv("GITHUB_REF")?.let { ref ->
    if (ref.startsWith("refs/tags/v")) {
        version = ref.substringAfterLast("refs/tags/v")
    }
}

tasks.dokkaHtmlMultiModule.configure {
    removeChildTasks(listOf(projects.mobiusktUpdateGenerator.dependencyProject))
}
