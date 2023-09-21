# Compose

[Jetpack Compose](https://developer.android.com/jetpack/compose) and
[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
are supported with the `mobiuskt-compose` module.

## Creating a Loop

With Compose, loops are created with `rememberMobiusLoop`.

### Example

```kotlin
@Composable
fun MyScreenRoute() {
    val (model, eventConsumer) = rememberMobiusLoop(ScreenModel()) {
        Mobius.loop(MyScreenUpdate(), MyScreenHandler())
            .logger(SimpleLogger("MyScreen"))
    }
    
    MyScreen(
        model = model,
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

