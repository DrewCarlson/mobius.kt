# Native

## WorkRunners

### Default WorkRunners

Native targets will use a K/N `Worker` for Events and Effects.

See **[Configuration](../configuration.md)** to change the default `WorkRunner`s.

### Common Native

For all native targets, there are `WorkRunner` implementations backed by Kotlin's
[Worker](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.native.concurrent/-worker/) APIs.

```kotlin
// Create a WorkRunner backed by a new Worker instance
val workRunner = WorkRunners.nativeWorker()

// Optional: Provide details for the new Worker instance
val workRunner = WorkRunners.nativeWorker(
    name = "Worker-1",
    errorReporting = true,
)

// Alternatively, use an existing Worker Instance
val workRunner = WorkRunners.nativeWorker(myWorkerInstance)
```

### Apple

For Apple targets, the `DispatchQueueWorkRunner` backed by `dispatch_queue` is available.

```kotlin
val globalWorkRunner = DispatchQueueWorkRunner.global()

val mainWorkRunner = DispatchQueueWorkRunner.main()

val customWorkRuner = WorkRunners.fromDispatchQueue(myDispatchQueue)
```

## Memory Manager Support

Since Kotlin 1.7.20 enabled the [new memory manager](https://kotlinlang.org/docs/native-memory-manager.html) by default,
Mobius.kt has been updated to support it.
The last Mobius.kt version supporting the old memory manager is
[v1.0.0-rc01](https://github.com/DrewCarlson/mobius.kt/releases/tag/v1.0.0-rc01).
