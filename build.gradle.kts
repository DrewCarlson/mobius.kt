import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    dependencies {
        classpath(libs.agp)
        classpath(libs.atomicfu.plugin)
    }
}

@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompat) apply false
    alias(libs.plugins.kover)
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    yarn.lockFileDirectory = rootDir.resolve("gradle/kotlin-js-store")
}

// Required for doc publishing
System.getenv("GITHUB_REF")?.let { ref ->
    if (ref.startsWith("refs/tags/v")) {
        version = ref.substringAfterLast("refs/tags/v")
    }
}

tasks.koverXmlReport {
    excludes = listOf("kt.mobius.test.matcher.*")
}

tasks.dokkaHtmlMultiModule.configure {
    removeChildTasks(listOf(projects.mobiusktUpdateSpec.dependencyProject))
}
