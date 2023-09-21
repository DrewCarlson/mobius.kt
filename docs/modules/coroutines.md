# Coroutines

Coroutines and Flows are supported with the `mobiuskt-coroutines` module (See [Download](../download.md)).

**Note:** `Update` functions are always synchronous and there is no use for coroutines in them.

## Side Effects

The `subtypeEffectHandler` builder provides various coroutine based methods to handle Effects in whatever way your application requires.

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

    addLatestValueCollector<Effect.SubType5> {
        // Same as `addValueCollector` but previous invocations are
        // disposed when a new Effect instance is emitted.
        emitAll(createEventFlow())
    }

    // (Flow<Effect>) -> Flow<Event>
    addTransformer<Effect.SubType6> { effects ->
        // This allows freeform Flow operator usage for more advanced cases.
        effects.map { effect -> Event.Result() }
    }
}
```
 
A `SubtypeEffectHandler` can be used directly with the `FlowMobius` loop factory

```kotlin
val loopFactory = FlowMobius.loop(update, effectHandler)
```

Or with the standard `Mobius`/`MobiusLoop` builders with the `asConnectable()` extension

```kotlin
val loopFactory = Mobius.loop(update, effectHandler.asConnectable())
```
