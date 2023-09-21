# Defining Events and Effects

Event and Effect objects are very similar.
They play distinct roles within a [Mobius loop](../reference/mobius-loop.md), yet both are
[immutable](./immutability.md) data objects used as messages.
Due to this, events and effects are commonly created using the same pattern.
In this document, we collectively refer to them as "messages."

## Different ways to define messages

Message types are opaque to Mobius.kt, so you are responsible for defining what they are and their meaning.
The only constraint is that all message kind instances must have a single type they implement.
So all Events for a specific loop share a type, and all Effects share a type.

Messages can be defined in multiple ways:

### Enumerations

The most basic kind of message is an `enum`, `Int`, or possibly a `String`.
Such messages cannot hold any additional data, so this is the most limited approach suitable only for simple loops.
This type of message is used mostly for documentation and internal testing.

### Tagged object

Building on enumerations, tagged objects could be a simple data model with a `tag` field defining the message type,
indicating what fields should be read.

You might also have a `Map<String, String>` holding data, each message may contain a different set of keys and values.
This is a valid approach and can simplify certain use-cases, but it lacks type-safety.
You must ensure you're creating maps with the correct data, and only reading the correct data when available.

### Subclasses

This is the recommended approach for defining messages.
Each message has a common parent type, like an interface `MyEvent`, which each Event type would implement.
The individual subclasses can then include their own specific data.

#### Kotlin Sealed Classes

With Kotlin, [sealed classes](https://kotlinlang.org/docs/reference/sealed-classes.html) greatly simplify creating
message structures.
They allow tying all the sub messages together like an `enum`, but allowing for instances with custom data.

```kotlin
sealed class MyEvent {
    data class Text(val text: String) : MyEvent()
    data class Number(val number: Int) : MyEvent()
    data object Reset : MyEvent()
}
```

!!! hint "Use `data object` when necessary"

    If the message type does not contain custom data, `data object` should be used instead of `data class`.

Now your Update function can use a `switch` statement to handle all message types:

```kotlin
when (event) {
    is Text -> event.text
    is Number -> event.number
    Reset -> /* code */
}
```

!!! warning "Avoid using `else ->` branches"

    When possible, avoid using an `else ->` branch to ensure all message types are handled when defining new ones.
