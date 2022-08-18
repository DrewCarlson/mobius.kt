plugins {
    kotlin("jvm")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview")
            optIn("kt.mobius.gen.ExperimentalUpdateGenerator")
        }
    }
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktUpdateGeneratorApi)
}
