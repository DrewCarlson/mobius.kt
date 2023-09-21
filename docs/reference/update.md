# Update

## Overview

Update is an interface with a single `update(model, event): Next<Model, Effect>` method.
It is simply referred to as the Update function.
Update functions must be [pure](../patterns/pure-vs-impure-functions.md), meaning there are some rules to follow:

## Update functions are pure

**Update functions must not have side-effects**

Do not mutate shared memory, interact with disk, log messages, make network requests, etc.
If the function is called, it should not change any part of the application.
The function should only dispatch Effects describing external work that should be executed.

**Update functions must not depend on external state**

The only things that determine the return value of an Update function are the Model and Event parameters.
Like before this means not interacting with disk, shared memory, using `Random` or using the system time.
You can also use constants alongside the current Model and Event objects.

**Configuration of the Update function must be in the Model.**

Update functions must not have member fields that cause the function to produce different results, even if they are
immutable.
Any configuration for the Update function should be stored in the Model.

## Update functions return a `Next` object

The `Next` class has four states: no change, only Effects, new Model, or both a new Model and effects.

The various values can be created with the following factories:

```kotlin
Next.noChange()

Next.next(model)

Next.next(model, effects)

Next.dispatch(effects)
```

When no Effects are provided, the Update will not emit any effects.
If there is no Model provided, then no Model will be emitted to observers.

## Guidelines for Update functions

**Start by defining expected behaviours of the Update function.**

Begin by creating unit tests for the Update function.
This will help you catch errors and tricky situations in your specifications early, before investing time in developing
the Update function, Effect Handlers, and UI.

**Mobius is a FP pattern.**

Mobius draws inspiration from functional programming (FP), but it's not always ideal to write code in a purely
functional style. There are situations where using a simple for-loop and appending to an array can be more
straightforward than employing a higher-order transform function with an anonymous class instance.
It's essential to prioritize simplicity and readability in your code and avoid unnecessary complexity.

**Use mutable data structures inside the Update function.**

The Update function must only be pure form the outside.
If temporary mutable structures can simplify your code, you're encouraged to do so.

**Large Update functions are not always a bad thing.**

If the control flow of your Update function is easy to understand, it can become quite large without becoming
difficult to reason about.
However, it may be beneficial to move individual event handling code into smaller static functions.

See [Modules > Codegen](../modules/codegen.md) for utilities to reduce Update function size.

**Avoid nesting too deeply.**

An Update function can lead to deeply nested code like when you `switch` on the Event, then `switch` on the current
state, and then add code to produce a result.
In such cases, using smaller functions to handle specific states and Events can be helpful along with other
techniques like early returns and local variables for intermediate state.
