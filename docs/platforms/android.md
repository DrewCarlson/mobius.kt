# Android

Mobius.kt provides a few different Android specific utilities for Logging and binding to Android UI.

_See the [Modules > Compose](../modules/compose.md) section for Jetpack Compose/Compose Multiplatform documentation._

## MobiusController

_(See [Android ViewModel](#ViewModel) below for a less manual approach to UI lifecycle binding.)_

`MobiusLoop.Controller`s are a useful construct for binding a `MobiusLoop` to platform specific UI lifecycles.
The `MobiusLoop.Controller` wraps a `MobiusLoop` so that it can be started/stopped and paused/resumed based on lifecycle events.
It can easily be bound to a `Activity`, `Fragment`, `View` or similar custom container.

```kotlin
// Create a LoopFactory as normal
val defaultModel = Model()
val loopFactory =
    Mobius.loop(update, effectHandler)
        .init(initFunc)
        .logger(AndroidLogger.tag("My Loop"))


val controller = MobiusAndroid.controller(loopFactory, defaultModel)
```

Now you have a `MobiusLoop.Controller` which can be bound to your desired UI abstraction.
The important detail in `MobiusAndroid.controller` is that model update handling is bound to the main thread.

See [Getting Started > Starting the Loop > Controller](../getting-started.md#controller) and [WorkRunners](#WorkRunners) for more details.


## ViewModel

To simplify lifecycle concerns with the standard Jetpack library approach, `MobiusLoopViewModel` is provided to wrap
a `MobiusLoop` instance inside a `ViewModel`.
This brings the lifecycle handling of your `MobiusLoop` directly inline with a standard `ViewModel` implementation.

```kotlin
val viewModel = MobiusLoopViewModel.create({ _, _ ->
    Mobius.loop(update, effectHandler)
        .init(initFunc)
}, defaultModel)

// Get the current model
viewModel.model

// Get a LiveData<Model>
viewModel.models

// Dispatch events to the loop
viewModel.dispatchEvent(Event())
```

## AndroidLogger

For Android, you'll likely want to log messages to Logcat.
The `AndroidLogger` implementation of `MobiusLoop.Logger` can be used as follows.

```kotlin
val logger = AndroidLogger.tag("My Loop")
val loopFactory =
    Mobius.loop(update, effectHandler)
        .init(initFunc)
        .logger(logger)
```

## WorkRunners

Two Android specific `WorkRunner` implementations are provided: `LooperWorkRunner` and `MainThreadWorkRunner`.
Additionally, Android targets have access to the standard Jvm `WorkRunner`s (see [Jvm > WorkRunners](jvm.md#WorkRunners)).

### MainThreadWorkRunner

As the name implies, `MainThreadWorkRunner` allows you to execute work on Android's Main Thread.
This is particularly useful for handling Model updates on the Main Thread in order to update UI elements.

```kotlin
val workRunner = MainThreadWorkRunner.create()
```

Note that when using `MobiusLoopViewModel` or `MobiusAndroid.controller`, you will not need to manually create a
`MainThreadWorkRunner`.

### LooperWorkRunner

`LooperWorkRunner` enables dispatching work to a provided `Looper`.
This is primarily used to facilitate the `MainTHreadWorkRunner`, but if needed any `Looper` can be provided.

```kotlin
val handlerThread = HandlerThread("MyThread")
handlerThread.start()

val workRunner = LooperWorkRunner.using(handlerThread.looper)
```