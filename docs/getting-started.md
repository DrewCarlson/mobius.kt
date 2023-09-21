# Getting Started

This page outlines the various Mobius.kt components in their simplest form, view the reference pages for additional details.

## Creating a Loop

(_See **[Reference > Mobius Loop](reference/mobius-loop.md)**_)

Let's create a simple Counter to see the various Mobius.kt components.
The first step in creating a loop is to define our `Model`, `Event`s, and `Effect`s.

### Model

(_See **[Reference > Model](reference/model.md)**_)

For simplicity, our Model will just be an `Int`:

```kotlin
typealias Model = Int
```

### Events

(_See **[Reference > Event](reference/event.md)**_)

Next we need Events to update our model:

```kotlin
enum class Event { ADD, SUB, RESET }
```

### Effects

(_See **[Reference > Effect](reference/effect.md)**_)

Effects will be covered later, so we'll just use `Unit` for now:

```kotlin
typealias Effect = Unit
```

### Update Function

(_See **[Reference > Update](reference/update.md)**_)

Now that we have a model and some events to handle, let's add some counter logic in an `Update` function.

```kotlin
val update = Update<Model, Event, Effect> { model, event ->
  when (event) {
      Event.ADD -> next(model + 1)
      Event.SUB -> next((model - 1).coerceAtLeast(0))
      Event.RESET -> next(0)
  }
}
```

### Effect Handler

(_See **[Reference > Effect Handler](reference/effect-handler.md)**_)

This example doesn't use any Effects, so we'll just define a no-op Effect Handler for now.

<details>
<summary>no-op effect handler (Click to expand)</summary>

```kotlin
val effectHandler = Connectable<Effect, Event> { output ->
    object : Connection<Effect> {
        override fun accept(value: Effect) = Unit
        override fun dispose() = Unit
    }
}
```
</details>

### Starting the Loop

That's it!  We've defined everything we need to construct a `MobiusLoop`.
The last thing we need is to construct a `MobiusLoop` instance with out components.

Here are two approaches to create the `MobiusLoop`, send it events, and handle model changes.

#### Manual

Using `Mobius.loop`, we create a `MobiusLoop.Factory` which can create a running `MobiusLoop`.

```kotlin
val loopFactory: MobiusLoop.Factory = Mobius.loop(update, effectHandler)

val loop: MobiusLoop = loopFactory.startFrom(0)
```

This is the simplest form of a `MobiusLoop`, it only has two states: `running` and `disposed`.

<details open="open">
<summary>Manual Loop Example</summary>

```kotlin
// Attach an Observer to handle model updates
val observerRef: Disposable = loop.observer { model ->
   println("Model: $model")
}

// Send some events to our loop
loop.dispatchEvent(Event.ADD)   // Model: 1
loop.dispatchEvent(Event.ADD)   // Model: 2
loop.dispatchEvent(Event.SUB)   // Model: 1
loop.dispatchEvent(Event.RESET) // Model: 0
loop.dispatchEvent(Event.SUB)   // Model: 0

// Cleanup our resources
loop.dispose()
```
</details>


#### Controller

Alternatively a loop can be managed with a `MobiusLoop.Controller`, giving the loop a flexible lifecycle.
This example includes some imaginary UI details for demonstration, this could apply to Android UI, iOS UIKit, or any
other UI framework.

<details open="open">
<summary>Loop Controller Example</summary>

```kotlin
// Create a controller that lives of our UI container
val loopController = Mobius.controller(loopFactory, 0)

// When our UI container is created, connect our buttons and outputs
loopController.connect { output ->
    buttonAdd.onClick { output.accept(Event.ADD) }
    buttonSub.onClick { output.accept(Event.SUB) }
    buttonReset.onClick { output.accept(Event.RESET) }
    
    object : Consumer<Model> {
        override fun accept(value: Model) {
            counterLabel.text = value.toString()
        }
     
        override fun dispose() {
            buttonAdd.removeOnClick()
            buttonSub.removeOnClick()
            buttonReset.removeOnClick()
        }
    }
}

// When the UI is presented: start the loop
loopController.start()

// When we click our buttons, the counterLabel will be updated with the new model

// When the UI is no longer presented: Stop the loop to prevent UI updates or events
loopController.stop()

// Loop could be started with `loopController.start()` when the UI is presented again

// When the UI is destroyed: Dispose the loop and release references to UI elements
loopController.disconnect()
```
</details>