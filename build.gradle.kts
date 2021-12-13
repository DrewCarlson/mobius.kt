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
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version BINARY_COMPAT_VERSION apply false
    id("org.jetbrains.kotlinx.kover") version KOVER_VERSION
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$DOKKA_VERSION")
}
