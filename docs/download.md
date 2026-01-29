# Download

[![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/mobiuskt-core-jvm?label=maven&color=blue)](https://central.sonatype.com/search?q=mobiuskt-*&namespace=org.drewcarlson)
![Sonatype Nexus (Snapshots)](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fdrewcarlson%2Fmobiuskt-core-jvm%2Fmaven-metadata.xml&label=nexus)

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Js&color=blue)
![](https://img.shields.io/static/v1?label=&message=Wasm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Jvm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Linux&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=Windows&color=blue)
![](https://img.shields.io/static/v1?label=&message=iOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=tvOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=watchOS&color=blue)

## Repository

Releases are published to Maven Central and snapshots are published to Sonatype OSS. Make sure the required repository is in your build script:

```kotlin
repositories {
    mavenCentral()
    // Or snapshots
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}
```

## Kotlin Gradle Script

```kotlin

val mobiusktVersion = "{{ lib_version }}"

dependencies {
    implementation("org.drewcarlson:mobiuskt-core:$mobiusktVersion")
    implementation("org.drewcarlson:mobiuskt-test:$mobiusktVersion")
    implementation("org.drewcarlson:mobiuskt-extras:$mobiusktVersion")
    implementation("org.drewcarlson:mobiuskt-coroutines:$mobiusktVersion")
    implementation("org.drewcarlson:mobiuskt-compose:$mobiusktVersion")
    
    // Update Spec Generator:
    implementation("org.drewcarlson:mobiuskt-codegen-api:$mobiusktVersion")
    ksp("org.drewcarlson:mobiuskt-codegen:$mobiusktVersion")
}
```

## Version Catalog (toml)

```toml
[versions]
mobiuskt = "{{ lib_version }}"

[libraries]
mobiuskt-core = { module = "org.drewcarlson:mobiuskt-core", version.ref = "mobiuskt" }
mobiuskt-test = { module = "org.drewcarlson:mobiuskt-test", version.ref = "mobiuskt" }
mobiuskt-extras = { module = "org.drewcarlson:mobiuskt-extras", version.ref = "mobiuskt" }
mobiuskt-coroutines = { module = "org.drewcarlson:mobiuskt-coroutines", version.ref = "mobiuskt" }
mobiuskt-compose = { module = "org.drewcarlson:mobiuskt-compose", version.ref = "mobiuskt" }
mobiuskt-codegen = { module = "org.drewcarlson:mobiuskt-codegen", version.ref = "mobiuskt" }
mobiuskt-codegen-api = { module = "org.drewcarlson:mobiuskt-codegen-api", version.ref = "mobiuskt" }
```