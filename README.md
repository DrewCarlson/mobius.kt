# Mobius.kt

[![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/mobiuskt-core-jvm?label=maven&color=blue)](https://search.maven.org/search?q=g:org.drewcarlson%20a:mobiuskt-*)
![](https://github.com/DrewCarlson/mobius.kt/workflows/Tests/badge.svg)
![Codecov](https://img.shields.io/codecov/c/github/drewcarlson/mobius.kt?token=7DKJUD60BO)

Kotlin Multiplatform framework for managing state evolution and side-effects, based on [spotify/Mobius](https://github.com/spotify/mobius).

## What is Mobius?

The core construct provided by Mobius is the Mobius Loop, best described by the official documentation. _(Embedded below)_

A Mobius loop is a part of an application, usually including a user interface.
In a Spotify context, there is usually one loop per feature such as “the album page”, “login flow”, etc., but a loop can also be UI-less and for instance be tied to the lifecycle of an application or a user session.

### Mobius Loop

![Mobius Loop Diagram](https://raw.githubusercontent.com/wiki/spotify/mobius/mobius-diagram.png)

> A Mobius loop receives [Events](https://github.com/spotify/mobius/wiki/Event), which are passed to an [Update](https://github.com/spotify/mobius/wiki/Update) function together with the current [Model](https://github.com/spotify/mobius/wiki/Model).
> As a result of running the Update function, the Model might change, and [Effects](https://github.com/spotify/mobius/wiki/Effect) might get dispatched.
> The Model can be observed by the user interface, and the Effects are received and executed by an [Effect Handler](https://github.com/spotify/mobius/wiki/Effect-Handler).

'Pure' in the diagram refers to pure functions, functions whose output only depends on their inputs, and whose execution has no observable side effects.
See [Pure vs Impure Functions](https://github.com/spotify/mobius/wiki/Pure-vs-Impure-Functions) for more details.

_(Source: [Spotify/Mobius](https://github.com/spotify/mobius/) - [Concepts > Mobius Loop](https://github.com/spotify/mobius/wiki/Concepts/66d6eef10cd91002f780e141d71dd57e6adebe78#mobius-loop))_

By combining Mobius Loops with Kotlin's MPP features, mobius.kt allows you to write and test pure functions (application and/or business logic) in Kotlin and deploy them everywhere.
This leaves impure functions to be written in multiplatform Kotlin code or the target platform's primary language (Js, Java, Objective-c/Swift), depending on your use-case.

## Example

```kotlin
typealias Model = Int

enum class Event { ADD, SUB, RESET }

typealias Effect = Unit

val update = Update<Model, Event, Effect> { model, event ->
  when (event) {
      Event.ADD -> next(model + 1)
      Event.SUB -> next(model - 1)
      Event.RESET -> next(0)
  }
}

val effectHandler = Connectable<Effect, Event> { output ->
    object : Connection<Effect> {
        override fun accept(value: Effect) = Unit
        override fun dispose() = Unit
    }
}

val loopFactory = Mobius.loop(update, effectHandler)
```

To create a simple loop use `loopFactory.startFrom(model)` which returns a `MobiusLoop` with two states: running and disposed.

<details>
<summary>Simple Loop Example (Click to expand)</summary>

```kotlin
val loop = loopFactory.startFrom(0)

val observerRef: Disposable = loop.observer { model ->
   println("Model: $model")
}

loop.dispatchEvent(Event.ADD)   // Model: 1
loop.dispatchEvent(Event.ADD)   // Model: 2
loop.dispatchEvent(Event.RESET) // Model: 0
loop.dispatchEvent(Event.SUB)   // Model: -1

loop.dispose()
```
</details>

Alternatively a loop can be managed with a `MobiusLoop.Controller`, giving the loop a more flexible lifecycle.

<details>
<summary>Loop Controller Example (Click to expand)</summary>

```kotlin
val loopController = Mobius.controller(loopFactory, 0)

loopController.connect { output ->
    buttonAdd.onClick { output.accept(Event.ADD) }
    buttonSub.onClick { output.accept(Event.SUB) }
    buttonReset.onClick { output.accept(Event.RESET) }
    
    object : Consumer<Model> {
        override fun accept(value: Model) {
            println(value.toString())
        }
     
        override fun dispose() {
            buttonAdd.removeOnClick()
            buttonSub.removeOnClick()
            buttonReset.removeOnClick()
        }
    }
}

loopController.start()

loopController.dispatchEvent(Event.ADD)   // Output: 1
loopController.dispatchEvent(Event.ADD)   // Output: 2
loopController.dispatchEvent(Event.RESET) // Output: 0
loopController.dispatchEvent(Event.SUB)   // Output: -1

loopController.stop()

// Loop could be started again with `loopController.start()`

loopController.disconnect()
```
</details>

## Modules

### Testing

The `mobiuskt-test` module provides a DSL for behavior driven tests and a light re-implementation of Hamcrest style APIs to test mobius loops (See [Download](#Download)).

<details>
<summary>Behavior testing DSL Example (Click to expand)</summary>

```kotlin
// Note that `update` is from the README example above
UpdateSpec(update)
    .given(0) // given model of 0
    .whenEvent(Event.ADD) // when Event.Add occurs
    .then(assertThatNext(hasModel())) // assert the Next object contains any model
// No AssertionError, test passed.

UpdateSpec(update)
    .given(0)
    .whenEvent(Event.ADD)
    .then(assertThatNext(hasModel(-1)))
// AssertionError: expected -1 but received 1, test failed.
```
</details>

For more details on the available matchers, see the [API documentation](https://drewcarlson.github.io/mobius.kt/mobiuskt-test/kt.mobius.test/-next-matchers/index.html).

### Coroutines

Coroutines and Flows are supported with the `mobiuskt-coroutines` module (See [Download](#Download)).

<details>
<summary>Coroutine Module Example (Click to expand)</summary>

```kotlin
val effectHandler = subtypeEffectHandler<Effect, Event> {
     // suspend () -> Unit
     addAction<Effect.SubType1> { }

     // suspend (Effect) -> Unit
     addConsumer<Effect.SubType2> { effect -> } 

     // suspend (Effect) -> Event
     addFunction<Effect.SubType3> { effect -> Event.Result() }

     // FlowCollector<Event>.(Effect) -> Unit
     addValueCollector<Effect.SubType4> { effect ->
         emit(Event.Result())
         emitAll(createEventFlow())
     }

     addLatestValueCollector<Effect.SubType5> {
         // Like `addValueCollector` but cancels the previous
         // running work when a new Effect instance arrives.
     }

     // Transform Flow<Effect> into Flow<Event>
     addTransformer<Effect.SubType6> { effects ->
         effects.map { effect -> Event.Result() }
     }
}

val loopFactory = FlowMobius.loop(update, effectHandler)
```
</details>


### Update Spec

Using [KSP](https://github.com/google/ksp/), `mobiuskt-update-spec` provides code generation to reduce manual boilerplate when writing complex `Update` functions.
Given a `sealed class Event` declaration, this module generates an interface defining update methods for each `Event` subclass and an exhaustive `when` block in the `update` method.

Take the following loop components as an example with the `@UpdateSpec` annotation applied to the Model class:
<details>
<summary>Loop components (Click to expand)</summary>

```kotlin
@UpdateSpec(
    eventClass = TestEvent::class,
    effectClass = TestEffect::class,
)
data class TestModel(
    val counter: Int,
)

sealed class TestEvent {
    object Increment : TestEvent()
    object Decrement : TestEvent()
    data class SetValue(val newCounter: Int) : TestEvent()
}

sealed class TestEffect
```
</details>

<details>
<summary>Generated output (Click to expand)</summary>

```kotlin
interface TestUpdateSpec : Update<TestModel, TestEvent, TestEffect> {
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

Use the following kts gradle configuration to apply the Update spec generator in your project:

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
    implementation("org.drewcarlson:mobiuskt-update-spec-api:$mobiuskt_version")
    ksp("org.drewcarlson:mobiuskt-update-spec:$mobiuskt_version")
}
```
</details>

<details>
<summary>Kotlin Gradle Script - Multiplatform (Click to expand)</summary>

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
                implementation("org.drewcarlson:mobiuskt-update-spec-api:$mobiuskt_version")
            }
        }
    }
}

// Note this must be in a top-level `dependencies` block, not `kotlin { sourceSets { .. } }`
dependencies {
    add("kspMetadata", "org.drewcarlson:mobiuskt-update-spec:$mobiuskt_version")
}

// This hack ensures that when compiling for any target, your `commonMain`
// sources are scanned and code is generated to `build/generated/ksp/commonMain`
// instead of a directory for the specific target.
// See https://github.com/google/ksp/issues/567#issuecomment-955035157
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name != "kspKotlinMetadata") {
        dependsOn("kspKotlinMetadata")
    }
}
```
</details>

For more details see the official [KSP documentation](https://github.com/google/ksp/blob/main/docs/kmp.md).


## Notes

### External dependencies

Mobius.kt depends on [kotlinx.atomicfu](https://github.com/Kotlin/kotlinx.atomicfu) for object synchronization, this results in a runtime dependency for Kotlin/Native targets only.

### Language Support

`MobiusLoop`s can be created and managed in Javascript, Swift, and Java code without major interoperability concerns.
Using Mobius.kt for shared logic does not require consuming projects to be written in or know about Kotlin.

### Kotlin/Native

Kotlin/Native's [new memory manager](https://blog.jetbrains.com/kotlin/2021/08/try-the-new-kotlin-native-memory-manager-development-preview/) is generally supported but as of Kotlin 1.6.10 may result in higher memory usage and in rare cases, delayed runtime errors.
The following notes are relevant only to the original memory manager where state shared across threads cannot be mutated.

A `MobiusLoop` is single-threaded on native targets and cannot be [frozen](https://kotlinlang.org/docs/native-immutability.html).
Generally this is acceptable behavior, even when the loop exists on the main thread.
If required, Effect Handlers are responsible for passing `Effect`s into and `Event`s out of a background thread.

Coroutines and Flows are ideal for handing Effects in the background with the [`mobiuskt-coroutines`](#Coroutines) module or manual example below.

<details>
<summary>Coroutine Example (Click to expand)</summary>

```kotlin
Connectable<Effect, Event> { output: Consumer<Event> ->
    object : Connection<Effect> {
        // Use a dispatcher for the Loop's thread, i.e. Dispatcher.Main
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        private val effectFlow = MutableSharedFlow<Effect.Subtype2>(
            onBufferOverflow = BufferOverflow.SUSPEND
        )
     
        init {
            effectFlow
                 .debounce(200)
                 .mapLatest { effect -> handleSubtype2(effect) }
                 .onEach { event -> output.accept(event) }
                 .launchIn(scope)
        }

        override fun accept(value: Effect) {
            scope.launch {
                when (value) {
                    is Effect.Subtype1 -> output.accept(handleSubtype1(value))
                    is Effect.Subtype2 -> effectFlow.emit(value)
                }
            }
        }
     
        override fun dispose() {
            scope.cancel()
        }
     
        private suspend fun handleSubtype1(effect: Effect.Subtype1): Event {
            return withContext(Dispatcher.Default) {
                // Captured variables are automatically frozen, DO NOT access `output` here!
                try {
                    val result = longRunningSuspendFun(effect.data)
                    Event.Success(result)
                } catch (e: Throwable) {
                    Event.Error(e)
                }
            }
        }
     
        private suspend fun handleSubtype2(effect: Effect.Subtype2): Event {
            return withDispatcher(Dispatcher.Default) {
                try {
                    val result = throttledSuspendFun(effect.data)
                    Event.Success(result)
                } catch (e: Throwable) {
                    Event.Error(e)
                }
            }
        }
    }
}
```
</details>

## Download

[![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/mobiuskt-core-jvm?label=maven&color=blue)](https://search.maven.org/search?q=g:org.drewcarlson%20a:mobiuskt-*)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.drewcarlson/mobiuskt-core-jvm?server=https%3A%2F%2Fs01.oss.sonatype.org)

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Js&color=blue)
![](https://img.shields.io/static/v1?label=&message=Jvm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Linux&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=Windows&color=blue)
![](https://img.shields.io/static/v1?label=&message=iOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=tvOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=watchOS&color=blue)

```kotlin
repositories {
    mavenCentral()
    // Or snapshots
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.drewcarlson:mobiuskt-core:$MOBIUS_VERSION")
    implementation("org.drewcarlson:mobiuskt-test:$MOBIUS_VERSION")
    implementation("org.drewcarlson:mobiuskt-extras:$MOBIUS_VERSION")
    implementation("org.drewcarlson:mobiuskt-coroutines:$MOBIUS_VERSION")
    
    // Update Spec Generator:
    implementation("org.drewcarlson:mobiuskt-update-spec-api:$mobiuskt_version")
    ksp("org.drewcarlson:mobiuskt-update-spec:$mobiuskt_version")
}
```
