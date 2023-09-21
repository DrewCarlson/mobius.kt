# Defining Models

Like [Events](../reference/event.md) and [Effects](../reference/effect.md), [Models](../reference/model.md) are opaque
to Mobius.
The only requirement is that they are [immutable](immutability.md).

Since the [Update](../reference/update.md) function represents state transitions in a state machine,
where the model represents the current state of the machine.
When defining the model of a state machine, there are many options for defining it between
a [finite-state machine](https://en.wikipedia.org/wiki/Finite-state_machine) and a less strict object containing
a number of fields that encapsulate state.

### All states use different classes

In the finite-state machine approach, one class per state means the machine can only be in one state at a time.
This means each state would only hold data needed for that state.

```kotlin
sealed class Model {
    data object WaitingForData : Model()
    data class Loaded(val data: String) : Model()
    data class Error(val message: String) : Model()
}
```

At any given moment, the model can only be one of the three `WaitingForData`, `Loaded`, and `Error` classes.
This approach is great for small loops with a few states, or to ensure all edge cases are handled.

This approach has a few drawbacks, particularly when there are a lot of states with overlapping data.
For example, when maintaining an "offline" state, you may need to differentiate offline-without-data from
offline-with-data.
You'll find this results in a large number of individual states that require their own transitions to be defined.

### All states use the same class

This approach is less strict in terms of a state machine, with all data being stored in top-level fields of the model.

```kotlin
data class Model(
    val loaded: Boolean,
    val error: Boolean,
    val offline: Boolean,
    val data: String?,
    val errorMessage: String?,
)
```

!!! warning

    With this approach you're likely to end up with a lot of `null` fields.
    The could also be invalid combinations of fields for example if both `loaded` and `error` are true,
    or both the `data` and `errorMessage` are populated.
    Be careful when using this approach as you must properly consider various cases of different field states.

It is generally best to start with this approach when defining model as it is easy to evolve with new requirements.

### Hybrid approach

By combining both previous approaches, we can get the best of both worlds: clear separation of data available in a
given state, and reduced effort when evolving the model.

```kotlin
sealed class LoadingState {
    data object WaitingForData : LoadingState()
    data class Loaded(val data: String) : LoadingState()
    data class Error(val message: String) : LoadingState()
}

data class Model(
    val offline: Boolean,
    val loading: LoadingState,
)
```

In this example we can have both the `Loaded` data and be `offline` at the same time.
This provides a scalable foundation for more complex loop behaviors while remaining easy to reason about.

## Some useful tricks for model objects

**Use `data class` for Models**

Kotlin [data classes](https://kotlinlang.org/docs/data-classes.html) provide useful utilities for Immutable objects.
Perhaps the most important is the generated `copy(...)` method, allowing you to create a new instance with only
specified fields changed.

```kotlin
data class Task(val description: String, val complete: Boolean)

val task1 = Task("hello", false)
val task2 = task1.copy(complete = true)
```

!!! note "Use `with`-methods to manage copy complexity"

    For certain data classes, some usages of `copy` may become large and difficult to comprehend quickly.
    In these cases it is helpful to add specialized `with` functions to produce new model instances.
    
    ```kotlin
    data class Model(
        val filter: Filter,
        val tasks: List<Task>,
    ) {
        
        fun withCompletedTask(completedTask: Task): Model {
            val newTasks = tasks.toMutableList()
            val taskIndex = newTasks.indexOf(completedTask)
            newTasks[taskIndex] = newTask[taskIndex].copy(completed = true)
            return copy(tasks = newTasks.toList())
        }
    }
    ```
