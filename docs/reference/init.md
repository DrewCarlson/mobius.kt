# Init

## Overview

`init(model)` functions are called when a Loop is started, they are like [Update](update.md) functions but are only
invoked to initialize the loop.
It takes the `startModel` and returns `First` which always has a [Model](model.md) and possibly a set
of [Effects](effect.md).

Init functions are especially useful to "resume" a new loop instance from a previous Model.
For example if the first loop instance is stopped in a `Loading` state, the associated effect being performed is lost.
The Init function could change the state from `Loading` to `Idle` or send an effect to restart the work.

!!! warning

    Providing a custom `Init` function is optional, but you must ensure the loop does not start in an unrecoverable
    state for the user.

## Example

```kotlin
val myInit = Init { model ->
    if (model.isLoading) {
        first(model, Effect.LoadData)
    } else {
        first(model)
    }
}
```

## Guidelines for the Init function

`Init` functions follow the same guidelines as [`Update` functions](update.md).
