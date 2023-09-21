# Pure vs Impure Functions

A [pure function](https://en.wikipedia.org/wiki/Pure_function) is a function that has a return value
determined entirely by the input parameters, and does not have any side-effects.

Functions that do not meet these requirements are called 'impure'.

## Pure functions are fundamentally simple

Because Pure functions are deterministic they are easy to understand, and makes issues easy to fix.
They are easy to use as building blocks when creating complex business logic.

## Pure functions are extremely easy to test

It is much easier to write an exhaustive test suite covering all input parameters that reach different branches.

## Impure functions are complex with internal state

Unlike Pure functions, Impure functions are harder to follow and test because they use external state and behavior that
affects the result of the function.

## More about Pure functions

For more information on benefits of pure functions, see
[https://alvinalexander.com/scala/fp-book/benefits-of-pure-functions/](https://alvinalexander.com/scala/fp-book/benefits-of-pure-functions/).