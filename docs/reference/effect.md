# Effect

## Overview

Effect objects allow [Update](update.md) functions to trigger execution of impure code in
an [Effect Handler](effect-handler.md).
[Impure](../patterns/pure-vs-impure-functions.md) code is any code that has side-effects which prevent the result from
being identical given the same parameters.
Such code might involve accessing backend HTTP services or reading from a local database.

Effect objects are immutable data structures like [`Events`](event.md) and [`Models`](model.md).
Both Effects and Events function as messages, but they differ in their purpose within the Update function.
Events represent occurrences that require a response from the business logic, while Effects signify actions that the
business logic intends to initiate in the external world.

## Execution Order

!!! warning "Execution order is not guaranteed"

    Mobius.kt does not make any Effect execution order guarantees.
    Including cases where two `Events` trigger `Effects`, the resulting `Effects` could execute in any order.

## Guidelines for Effects

- Use imperative naming conventions that convey the intended action, such
  as `SubmitLoginRequest`, `SaveUserToDisk`, `LoadListData`, etc.

- Ensure Effects are as value objects without business logic.

- Use `sealed class` in Kotlin for defining Effect types.