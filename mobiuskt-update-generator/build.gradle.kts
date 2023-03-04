plugins {
    kotlin("jvm")
    alias(libs.plugins.mavenPublish)
}

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
