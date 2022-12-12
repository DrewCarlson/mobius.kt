plugins {
    kotlin("jvm")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("kt.mobius.autowire.ExperimentalAutoWire")
        }
    }
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.coroutines.core)
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktCoroutines)
    implementation(projects.mobiusktAutowireApi)
}
