buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:$AGP_VERSION")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$ATOMICFU_VERSION")
    }
}

plugins {
    kotlin("multiplatform") version KOTLIN_VERSION apply false
    id("org.jetbrains.dokka") version DOKKA_VERSION
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    System.getenv("GITHUB_REF")?.let { ref ->
        if (ref.startsWith("refs/tags/")) {
            version = ref.substringAfterLast("refs/tags/")
        }
    }
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$DOKKA_VERSION")
}
