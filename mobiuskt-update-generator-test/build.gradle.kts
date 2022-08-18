@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

sourceSets {
    main { java.srcDir(buildDir.resolve("generated/ksp/$name/kotlin")) }
}

dependencies {
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktUpdateGeneratorApi)
    ksp(projects.mobiusktUpdateGenerator)
}
