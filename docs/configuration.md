# Configuration

## Default Work Runners

Safe defaults are provided for Event and Effect `WorkRunner`s for all platforms, but you can also provide custom defaults to simplify loop creation:

```kotlin
MobiusHooks.setDefaultEventRunner { WorkRunners.singleThread() }
MobiusHooks.setDefaultEffectRunner { WorkRunners.fixedThreadPool(4) }

// New loops will use the provided defaults unless overridden in the builder:
val loopFactory = Mobius.loop(update, effectHandler)
val loop = loopFactory.startFrom(model)
```

See the platform references to understand the default `WorkRunners`:

- **[Platforms > Jvm](platforms/jvm.md)**
- **[Platforms > Android](platforms/android.md)**
- **[Platforms > Native](platforms/native.md)**
- **[Platforms > Javascript](platforms/javascript.md)**