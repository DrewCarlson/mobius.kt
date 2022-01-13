plugins {
    kotlin("jvm")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    target.compilations.all {
        languageSettings.optIn("com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview")
    }
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktUpdateSpecApi)
}
