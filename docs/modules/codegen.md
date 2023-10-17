# Code Generation

## Update Generator

A common pain point with Mobius.kt is wrapping Event types to their state change functions.
In Mobius.kt this code is implemented in your `Update` function, taking an event and returning a `Next` instance.

Using [KSP](https://github.com/google/ksp/), `mobiuskt-codegen` provides code generation to reduce manual boilerplate when writing complex `Update` functions.

Given a `sealed class Event` declaration, an interface is generated defining update methods for each `Event` subclass and the exhaustive `when` block in the `update` method.

### Example

To apply code generation, add the `@GenerateUpdate` annotation to your Update function class definition:

```kotlin
@GenerateUpdate
class TestUpdate : Update<TestModel, TestEvent, TestEffect>, TestGeneratedUpdate {
  // ...
}
```

<details>
<summary>Loop components (Click to expand)</summary>

```kotlin
data class TestModel(
    val counter: Int,
)

sealed class TestEvent {
    data object Increment : TestEvent()
    data object Decrement : TestEvent()
    data class SetValue(val newCounter: Int) : TestEvent()
}

sealed class TestEffect
```
</details>

<details open="open">
<summary>Generated output</summary>

```kotlin
interface TestGeneratedUpdate : Update<TestModel, TestEvent, TestEffect> {
    override fun update(model: TestModel, event: TestEvent): Next<TestModel, TestEffect> {
        return when (event) {
            TestEvent.Increment -> increment(model)
            TestEvent.Decrement -> decrement(model)
            is TestEvent.SetValue -> setValue(model, event)
        }
    }
    
    fun increment(model: TestModel): Next<TestModel, TestEffect>
    
    fun decrement(model: TestModel): Next<TestModel, TestEffect>
    
    fun setValue(model: TestModel, event: TestEvent.SetValue): Next<TestModel, TestEffect>
}
```
</details>

## Nested sealed classes

By default, nested sealed Events will produce a function for each subtype.

<details open="open">
<summary>Child Sealed Class Default Behavior</summary>

```kotlin
sealed class Event {
    // ...
    sealed class Result : Event() {
        data class Success(val data: String) : Result()
        data class Error(val message: String) : Result()
    }
}
```

```kotlin
fun resultSuccess(event: Event.Result.Success): Next<Model, Effect> {
    // ...
}

fun resultError(event: Event.Result.Error): Next<Model, Effect> {
    // ...
}
```
</details>

This behavior can be changed with `@DisableSubtypeSpec`, causing the sealed class to be handled by one function.

<details open="open">
<summary>Child Sealed Class Default Behavior</summary>
```kotlin

sealed class Event {
    @DisableSubtypeSpec
    sealed class Result : Event() {
        // ...
```

```kotlin
fun result(event: Event.Result): Next<Model, Effect> {
    // ...
}
```
</details>


## Gradle Configuration

Use the following kts gradle configuration to apply the Update generator in your project:

<details>
<summary>Kotlin Gradle Script - JVM/Android (Click to expand)</summary>

```kotlin
plugins {
    kotlin("jvm") // or kotlin("android")
    id("com.google.devtools.ksp") version "<KSP-Version>"
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/$name/kotlin")
    }
}

dependencies {
    implementation("org.drewcarlson:mobiuskt-codegen-api:$mobiuskt_version")
    ksp("org.drewcarlson:mobiuskt-codegen:$mobiuskt_version")
}
```
</details>

<details open="open">
<summary>Kotlin Gradle Script - Multiplatform</summary>

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp") version "<KSP-Version>"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/$name/kotlin")
            dependencies {
                implementation("org.drewcarlson:mobiuskt-codegen-api:$mobiuskt_version")
            }
        }
    }
}

// Note this must be in a top-level `dependencies` block, not `kotlin { sourceSets { .. } }`
dependencies {
    add("kspCommonMainMetadata", "org.drewcarlson:mobiuskt-codegen:$mobiuskt_version")
}

// This ensures that when compiling for any target, your `commonMain` sources are
// scanned and code is generated to `build/generated/ksp/commonMain` instead of a
// directory for the specific target. See https://github.com/google/ksp/issues/567
if (tasks.any { it.name == "kspCommonMainKotlinMetadata" }) {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
        if (name != "kspCommonMainKotlinMetadata") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}
```
</details>

For more details see the official [KSP documentation](https://kotlinlang.org/docs/ksp-multiplatform.html).
