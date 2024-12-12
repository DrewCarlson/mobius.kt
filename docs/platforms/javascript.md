# Javascript

## WorkRunners

For Javascript targets the `AsyncWorkRunner` backed by [`setTimeout(work, 0)`](https://developer.mozilla.org/en-US/docs/Web/API/setTimeout) is provided.

```kotlin
val workRunner = WorkRunners.async()
```
### Default Runners

Javascript targets will use an async `WorkRunner` for Events and Effects.

See **[Configuration](../configuration.md)** to change the default `WorkRunner`s.
