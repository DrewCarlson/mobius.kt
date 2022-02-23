@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

sourceSets {
    main { java.srcDir(buildDir.resolve("generated/ksp/$name/kotlin")) }
}

dependencies {
    implementation(projects.mobiusktCore)
    implementation(projects.mobiusktGenerateUpdateApi)
    ksp(projects.mobiusktGenerateUpdate)
}
