plugins {
    kotlin("jvm")
    alias(libs.plugins.mavenPublish)
}

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("com.google.devtools.ksp.KspExperimental")
            optIn("kt.mobius.gen.ExperimentalCodegenApi")
        }
    }
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.compileTesting)
    implementation(libs.compileTesting.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktCodegenApi)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}
