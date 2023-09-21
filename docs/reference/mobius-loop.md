# Mobius Loop

## Overview

A Mobius loop processes [Events](event.md), which are then directed to an [Update](update.md) function.
This function can potentially alter the [Model](model.md) and dispatch [Effects](effect.md).
The user interface can monitor the Model, while an [Effect Handler](effect-handler.md) manages and carries out the Effects.

![Mobius Loop Diagram](https://raw.githubusercontent.com/wiki/spotify/mobius/mobius-diagram.png)

The Mobius Loop is the core API which connects all the Mobius components.
It invokes the Update function with Events, holds the current Model, sends Effects to an Effect Handler,
and observes the Event Source.

### Creation

A loop can be created using `Mobius.loop` to provide configuration and `startFrom(model)` to start it with an
initial Model.

```kotlin
val loop = Mobius.loop(Example::update, ::createEffectHandler)
    .startFrom(Model())
```

### Observing

Use `loop.observe(...)` to be notified of model changes:

```kotlin
val disposable = loop.observe(::onModelChanged)

// To stop receiving updates:
disposable.dispose()
```

Calling `dispose` on an observer is only required if the loop will remain running, otherwise `loop.dispose()`
will dispose of observers.
