# Effect Handler

## Overview

Effect Handlers execute [Effect](effect.md) messages and potentially produce [Events](event.md) as a result.

A [Mobius loop](mobius-loop.md) has a single Effect Handler, which is usually composed of individual Effect Handlers for
Effect type.
(See [Modules > Coroutines](../modules/coroutines.md) for how to compose Coroutine-based Effect Handlers.)

[Model](model.md) data should be passed to the Effect handler within the Effect object.
An Effect Handler could subscribe to Model updates at the cost of races and reduce simplicity.

!!! warning "Exceptions"

    Effect Handlers can **never** throw Exceptions, it will cause a crash or put the loop in an unusable state.
    If an error occurs while executing an Effect, it should be transformed into an `Event` to be processed by the `Update` function.

## Connections

Effect Handlers are connected to a `MobiusLoop` with a `Connection`, this allows the loop to send Effects to the
handler and signal when shut down is required.

Effect Handlers that can only be connected to once, which is generally the case, must throw
the `ConnectionLimitExceeded` exception to prevent difficult to identify bugs.
If an Effect Handler instance can be shared with multiple loops, ensure that it is prepared to handle it.
