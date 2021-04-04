# Mobius.kt

[![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/mobiuskt-core-jvm?label=maven&color=blue)](https://search.maven.org/search?q=g:org.drewcarlson%20a:mobiuskt-*)
![](https://github.com/DrewCarlson/mobius.kt/workflows/Jvm/badge.svg)
![](https://github.com/DrewCarlson/mobius.kt/workflows/Js/badge.svg)
![](https://github.com/DrewCarlson/mobius.kt/workflows/Native/badge.svg)

Kotlin Multiplatform [Mobius](https://github.com/spotify/mobius) implementation.

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

_(Source: [Spotify/Mobius](https://github.com/spotify/mobius/) - [Concepts > Mobius Loop](https://github.com/spotify/mobius/wiki/Concepts/53777574e070e168f2c3bdebc1be544edfcee2cf#mobius-loop))_

By combining this concept with Kotlin's MPP features, mobius.kt allows you to write and test all of your pure functions (application and/or business logic) in Kotlin and deploy it everywhere.
This leaves impure functions to the native platform, which can be written in their primary language (Js, Java, Objective-c/Swift) or in Kotlin!

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

At this point a loop is not running, `loopFactory` must be used to create a "raw" loop.
Raw loops have two states: running and disposed.


<details>
<summary>Show Raw Loop Example</summary>

```kotlin
val loop = loopFactory.startFrom(0)

val observerRef = loop.observer { model -> println(model.toString()) }

loop.dispatchEvent(Event.ADD)   // Output: 1
loop.dispatchEvent(Event.ADD)   // Output: 2
loop.dispatchEvent(Event.RESET) // Output: 0
loop.dispatchEvent(Event.SUB)   // Output: -1

observerRef.dispose() // Not required if calling loop.dispose() which disposes all observers.
loop.dispose()
```
</details>

Alternatively a loop can be managed with a `MobiusLoop.Controller`, giving the loop a more flexible lifecycle.


<details>
<summary>Show Loop Controller Example</summary>

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


## Notes

### Language Support

`MobiusLoop`s can be created and managed in Javascript, Swift, and Java code without major interoperability concerns.
Using Mobius.kt for shared logic does not require consuming projects to be written in or know about Kotlin.

### Kotlin/Native

A `MobiusLoop` is single-threaded on native targets and cannot be [frozen](https://kotlinlang.org/docs/native-immutability.html).
Generally this is acceptable behavior, even when the loop exists on the main thread.
If required, Effect Handlers are responsible for passing `Effect`s into and `Event`s out of a background thread.

Coroutines and Flows provide the best way to execute work on the background.

<details>
<summary>Show Coroutine Example</summary>

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
  implementation("org.drewcarlson:mobiuskt-extras:$MOBIUS_VERSION")
  implementation("org.drewcarlson:mobiuskt-android:$MOBIUS_VERSION")
}
```
