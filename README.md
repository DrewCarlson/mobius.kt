# kotlin-mobius
[![CircleCI](https://circleci.com/gh/DrewCarlson/kotlin-mobius.svg?style=shield&circle-token=5969d260c8b600d8fb1e55b633eabb7760fcea46)](https://circleci.com/gh/DrewCarlson/kotlin-mobius)

An experimental port of [Mobius](https://github.com/spotify/mobius) to Kotlin, with MPP support.

## Notes

**Jvm**: The Jvm module's should work just fine, they are _almost_ direct ports of Mobius minus a few kotlin related improvements.
The Jvm modules still need changes for better interop in Java and existing Kotlin projects that depend on Mobius.

**Js**: I have not done much testing with the Js module but it should work without any significant issues, mostly because Js is not subject to the same multi-threading considerations as other platforms.

**Native**: The Native side still needs a lot of work.. and tests.
In theory (and some practice), kotlin-mobius escapes some of Kotlin/Native's multi-threading considerations but only if you carefully consider every operation.

## Samples

The sample projects can be run with gradle from the `samples` directory.

### Todo

##### iOS Simulator
*Note: don't forget to deploy the native klib first!* `./gradlew :native:core-native:publishToMavenLocal`

Open and run the `samples/todo/ios` Xcode project, it contains build steps to produce the `Todo.framework`

## Download

Due to the experimental nature of the project, your best option is to publish artifacts locally.
For convenience JitPack can be used for the non-native modules.
You can easily publish a Kotlin/Native klib on your machine ([Consuming klibs](https://github.com/JetBrains/kotlin-native/blob/master/GRADLE_PLUGIN.md#dependencies)).

```groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
    // Optional for Native klib or local publishing
    mavenLocal()
  }
}
```

```groovy
dependencies {
  // Common
  implementation 'com.github.DrewCarlson.kotlin-mobius:core-common:master-SNAPSHOT'
  
  // Js
  implementation 'com.github.DrewCarlson.kotlin-mobius:core-js:master-SNAPSHOT'
  
  // Jvm
  implementation 'com.github.DrewCarlson.kotlin-mobius:core-jvm:master-SNAPSHOT'
  implementation 'com.github.DrewCarlson.kotlin-mobius:framework-android:master-SNAPSHOT'
  
  // Native (not on JitPack!)
  // run: `./gradlew :native:core-native:publishToMavenLocal`
  implementation 'com.github.DrewCarlson.kotlin-mobius:native-core:master-SNAPSHOT'  
}
```
