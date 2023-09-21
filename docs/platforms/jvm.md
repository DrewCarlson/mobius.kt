# Jvm

## WorkRunners

Various factories are provided for `java.util.concurrent.ExecutorService`.

### Single Thread

```kotlin
val workRunner = WorkRunners.singleThread()
```

### Fixed Thread Pool

```kotlin
val workRunner = WorkRunners.fixedThreadPool(n = 2)
```

### Cached Thread Pool

```kotlin
val workRunner = WorkRunners.cachedThreadPool()
```

### Existing ExecutorService

If you have an existing `ExecutorService`, you can wrap it with `WorkRunners.from(service)`

```kotlin
val myExecutor = Executors.newSingleThreadExecutor()
val workRunner = WorkRunners.from(myExecutor)
```
