# Logging and Error Handling

## Logging

`MobiusLoop` logging is handled by the `MobiusLoop.Logger` provided to the `MobiusLoop.Builder`.
Mobius.kt also has some internal logging which is handled by `MobiusHooks.InternalLogger`.

The default `InternalLogger` is backed by `println` for broad platform support.
A custom `InternalLogger` implementation can be provided by setting a logger factory:

```kotlin
MobiusHooks.setLoggerFactory { tag: String ->
    CustomInternalLogger(tag)
}
```

## Error Handling

Mobius.kt attempts to expose programmer errors as `RuntimeException`s resulting in a crash.
Because uncaught exceptions in background `Threads`/`Workers` are handled by the specific instance,
they are logged as an Error and ignored by Mobius.kt.

You can override the default error handling behavior with `MobiusHooks.setErrorHandler`:

```kotlin
MobiusHooks.setErrorHandler { error: Throwable ->
    MyCrashReportingService.logException(error)
    error.printStackTrace()
}
```
