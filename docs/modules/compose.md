# Compose

The `mobiuskt-compose` module provides support for
[Jetpack Compose](https://developer.android.com/jetpack/compose) and
[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).

## Creating a Loop

With Compose, loops are created with `rememberMobiusLoop`.

### Example

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

!!! note

    Create the loop outside the main Composable UI function.
    You should provide the model and event consumer function as parameters to maintain preview support.
    The loop setup would typically live at the same level as your navigation handler body for the associated route.
