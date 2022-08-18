@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("kt.mobius.gen.ExperimentalUpdateGenerator")
        }
    }
}

sourceSets {
    main { java.srcDir(buildDir.resolve("generated/ksp/$name/kotlin")) }
}

dependencies {
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktUpdateGeneratorApi)
    ksp(projects.mobiusktUpdateGenerator)
}
