# Extras

The extras module provides various flexible utilities for composing [Mobius loops](../reference/mobius-loop.md) in a way
that fits your project.

## CompositeLogger

Multiple `MobiusLoop.Logger` instances can be combined with a `CompositeLogger`.

```kotlin
val myLogger = CompositeLogger.from(
    MyLoggingBackendLogger("MyScreen"),
    SimpleLogger("MyScreen")
)

val loop = Mobius.loop(MyUpdate(), effectHandler)
    .logger(myLogger)
    .startFrom
```

## Effect Handler Decorators

### CompositeEffectHandler

`CompositeEffectHandler` delegates to a provided list of [Effect handlers](../reference/effect-handler.md).
This can be useful for reusing handlers with multiple loops, and keeping handlers small and focused.

```kotlin
val effectHandler = CompositeEffectHandler.from(
    Connectable { output -> AnalyticsHandler(output) },
    Connectable { output -> UserStateHandler(output) },
    Connectable { output -> MyScreenHandler(output) }
)

val loop = Mobius.loop(MyScreenUpdate(), effectHandler)
    .startFrom(MyScreenModel())
```

### MappedEffectHandler

`MappedEffectHandler` allows you to transform in the Effect inputs and Event outputs to fit your loop.

```kotlin

val myScreenUserStateHandler = Connectable { output -> UserStateHandler(output) }
    .mapped(
        mapEffect = { effect: MyScreenEffect ->
            when (effect) {
                MyScreenEffect.Logout -> UserStateEffect.Logout
                MyScreenEffect.RefreshUserState -> UserStateEffect.Refresh
                else -> null // Ignore the effect
            }
        },
        mapEvent = { event: UserStateEvent ->
            when (event) {
                UserStateEvent.LoggedOut -> MyScreenEvent.LoggedOut
                is UserStateEvent.UserUpdated -> MyScreenEvent.UserUpdated(event.user)
                else -> null // Ignore the event
            }
        }
    )
val effectHandler = CompositeEffectHandler.from(
    myScreenUserStateHandler,
    Connectable { output -> MyScreenHandler(output) }
)
```
