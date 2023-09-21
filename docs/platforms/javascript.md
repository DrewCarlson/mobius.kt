# Javascript

## WorkRunners

For Javascript targets the `AsyncWorkRunner` backed by [`setTimeout(work, 0)`](https://developer.mozilla.org/en-US/docs/Web/API/setTimeout) is provided.

```kotlin
val workRunner = WorkRunners.async()
```
