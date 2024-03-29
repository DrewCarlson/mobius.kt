# Coroutines

Coroutines and Flows are supported with the `mobiuskt-coroutines` module
(See [Download](../download.md)).

**Note:** `Update` functions are always synchronous and there is no use for coroutines in them.

## Side Effects

The `subtypeEffectHandler` builder provides various coroutine based methods to handle Effects in
whatever way your application requires.

```kotlin
val effectHandler = subtypeEffectHandler<Effect, Event> {

    // suspend () -> Unit
    addAction<Effect.SubType1> {
        // Perform action without Effect data and without a result.
    }

    // suspend (Effect) -> Unit
    addConsumer<Effect.SubType2> { effect ->
        // Perform action with Effect data and without a result.
    }

    // suspend (Effect) -> Event
    addFunction<Effect.SubType3> { effect ->
        // Perform action with Effect data and with a result.
        Event.Result()
    }

    // FlowCollector<Event>.(Effect) -> Unit
    addValueCollector<Effect.SubType4> { effect ->
        // Perform action with Effect data and `FlowCollector` body
        // Useful for tasks with multiple result events.
        emit(Event.Result())
        emitAll(createEventFlow())
    }

    // (Flow<Effect>) -> Flow<Event>
    addTransformer<Effect.SubType6> { effects ->
        // This allows freeform Flow operator usage for more advanced cases.
        effects.map { effect -> Event.Result() }
    }
}
```

### Creating the Loop

A `SubtypeEffectHandler` can be used directly with the `FlowMobius` loop factory

```kotlin
val loopFactory = FlowMobius.loop(update, effectHandler)
```

Or with the standard `Mobius`/`MobiusLoop` builders with the `asConnectable()` extension

```kotlin
val loopFactory = Mobius.loop(update, effectHandler.asConnectable())
```

### Execution Policy

Execution of functions added to a `SubtypeEffectHandler` can be configured with
an `ExecutionPolicy`.

- (Default) `ExecutionPolicy.Concurrent(concurrency: Int)`: Effects will be processed concurrently up to the maximum provided
  concurrency limit.
  The default limit is defined
  by [`DEFAULT_CONCURRENCY`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-d-e-f-a-u-l-t_-c-o-n-c-u-r-r-e-n-c-y.html)
  from the coroutines library.

- `ExecutionPolicy.Sequential`: The handler is executed with each Effect in order one at a
  time, waiting until the previous execution is complete before starting another.

- `ExecutionPolicy.Latest`: Each Effect will execute the handler, new Effects will cancel the
  previous handler if it has not finished executing.

- `ExecutionPolicy.ThrottleLatest(window: Duration)`: Immediately handle the first Effect, delaying any new effects
  by the provided window.  When a new Effect is dispatched within
  the window, it is dispatched after the window elapses and the
  previous handler is canceled if still running.

An `ExecutionPolicy` can be applied in two ways:

```kotlin
// Set the default policy for all handlers
subtypeEffectHandler<Effect, Event>(ExecutionPolicy.Sequential) {

    // Override the default per handler function
    addConsumer<Effect.MyEffect>(ExecutionPolicy.Latest) {
        // ...
    }
}
```
