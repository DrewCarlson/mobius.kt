# Testing

The `mobiuskt-test` module provides a DSL for behavior driven tests and a light re-implementation of Hamcrest style APIs to test mobius loops (See [Download](#Download)).

<details open="open">
<summary>Behavior testing DSL Example</summary>

```kotlin
@Test
fun testAddEvent() {
    UpdateSpec(update)
        .given(0) // given model of 0
        .whenEvent(Event.ADD) // when Event.Add occurs
        .then(assertThatNext(hasModel())) // assert the Next object contains any model
    // No AssertionError, test passed.
}

@Test
fun testAddEventError() {
    UpdateSpec(update)
        .given(0)
        .whenEvent(Event.ADD)
        .then(assertThatNext(hasModel(-1)))
    // AssertionError: expected -1 but received 1, test failed.
}
```
</details>

For more details on the available matchers, see the [API documentation](https://drewcarlson.github.io/mobius.kt/latest/kdoc/mobiuskt-test/kt.mobius.test/-next-matchers/index.html).
