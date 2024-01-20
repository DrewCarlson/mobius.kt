# Compose

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Android&color=blue)
![](https://img.shields.io/static/v1?label=&message=Js(HTML)&color=blue)
![](https://img.shields.io/static/v1?label=&message=Jvm&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=iOS&color=blue)

The `mobiuskt-compose` module provides support for
[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) and
[Jetpack Compose](https://developer.android.com/jetpack/compose) with built in support
for [Jetpack Navigation Component](https://developer.android.com/jetpack/compose/navigation)

_Note: This module is experimental and likely to change in the future._

## Creating a Loop

With Compose, loops are created with `rememberMobiusLoop`.

```kotlin
@Composable
fun MyScreenRoute() {
    val (modelState, eventConsumer) = rememberMobiusLoop(ScreenModel()) {
        Mobius.loop(MyScreenUpdate(), MyScreenHandler())
            .logger(SimpleLogger("MyScreen"))
    }
    
    MyScreen(
        model = modelState.value,
        eventConsumer = eventConsumer,
    )
}

@Composable
fun MyScreen(
    model: ScreenModel,
    modifier: Modifier = Modifier,
    eventConsumer: (ScreenEvent) -> Unit
) {
    Column(modifier = modifier) {
        Text(model.labelTest)
        
        Button(
            onClick = { eventConsumer(ScreenEvent.OnClick) }
        ) {
            Text("Button")
        }
    }
}
```

## Platform Behavior

### iOS/Desktop/Web

For these platforms, the loop is running while in the Composition and is disposed when removed.
The `rememberMobiusLoopLocal` method is available if you need to enforce this behavior on all
platforms.

### Android

When using [Jetpack Navigation Component](https://developer.android.com/jetpack/compose/navigation),
the loop will be scoped to the route and survive configuration changes.

Without Jetpack Navigation, `rememberMobiusLoop` uses `rememberMobiusLoopLocal` meaning the loop
will be disposed and recreated on configuration changes.

??? note "Supporting other Navigation libraries (Control the loop's lifecycle)"

    To support different navigation libraries, you must provide a custom `ViewModelStoreOwner` that
    is tied to the libraries route lifecycle.

    `rememberMobiusLoop` checks if `LocalViewModelStoreOwner.current` is set to an Activity,
    in which case `rememberMobiusLoopLocal` is used. When it's not an Activity, we're likely
    within a route for Jetpack Navigation or some other library so the loop will be held in
    a ViewModel which has it's lifecyle managed by the store owner.
    
    ```kotlin
    val navLibraryViewModelStoreOwner = ...
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides navLibraryViewModelStoreOwner
    ) {
        route(path = "my-screen") {
            val (modelState, eventConsumer) = rememberMobiusLoop(ScreenModel()) {
                Mobius.loop(MyScreenUpdate(), MyScreenHandler())
                    .logger(SimpleLogger("MyScreen"))
            }
            MyScreen(
                model = modelState.value,
                eventConsumer = eventConsumer,
            )
        }
    }
    ```
