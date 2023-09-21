# Event Source

## Overview

Event Sources send external [events](event.md) into a [`MobiusLoop`](mobius-loop.md).
You might have external events if you listen to:

- Network connection state changes like offline/limit data/etc.
- Hardware changes like bluetooth connected/disconnected
- System timers or periodic ticks
- etc.

Event Sources are like Effect Handlers but do not require [Effects](effect.md) before sending any Events.

!!! warning "Not for UI events"

    While it's possible to send UI events through an event source, you should instead use
    `MobiusLoop.dispatchEvent(event)` or a `Connectble` when using `MobiusLoop.Controller`.

## Usage

You can configure an event source with `.eventSource(...)` on a `MobiusLoop.Builder`:

```kotlin
val loopBuilder = Mobius.loop(update, effectHandler)
    .eventSource(myEventSource)
```

With Coroutines, you can wrap a `Flow<Event>` into an `EventSource<Event>`:

```kotlin
val first: Flow<Event.First> = // ...
val second: Flow<Event.Second> = // ...
val third: Flow<Event.Third> = // ...
val eventFlows = merge(first, second, third)

val eventSource = eventFlows.toEventSource()
```
