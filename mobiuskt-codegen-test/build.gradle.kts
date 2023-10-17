plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("kt.mobius.gen.ExperimentalCodegenApi")
        }
    }
}

sourceSets {
    main { java.srcDir(layout.buildDirectory.file("generated/ksp/$name/kotlin")) }
}

dependencies {
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktCodegenApi)
    ksp(projects.mobiusktCodegen)
}
