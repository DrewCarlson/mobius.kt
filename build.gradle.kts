buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")
        classpath("digital.wup:android-maven-publish:3.6.2")
    }
}

plugins {
    kotlin("multiplatform") version KOTLIN_VERSION apply false
    id("org.jetbrains.dokka") version DOKKA_VERSION
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }

    System.getenv("GITHUB_REF")?.let { ref ->
        if (ref.startsWith("refs/tags/")) {
            version = ref.substringAfterLast("refs/tags/")
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask> {
    if (!name.contains("html", ignoreCase = true)) return@withType

    val docs = buildDir.resolve("dokka/html")
    outputDirectory.set(docs)
    doLast {
        docs.resolve("-modules.html").renameTo(docs.resolve("index.html"))
    }
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$DOKKA_VERSION")
}
