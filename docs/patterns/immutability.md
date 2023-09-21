# Immutability

Mobius.kt requires most objects to be immutable, including the
[Model](../reference/model.md), [Event](../reference/event.md), and [Effect](../reference/effect.md) objects.
Without this constraint Mobius does not work.

An object is immutable if it and all objects it refers to do not have mutable fields.
Mutable fields are only safe if they do not change during the objects existence.

!!! warning "Beware of arrays"

    Arrays are mutable so having a `val myarray: Array<Int>` or `final Array<Int> myArray = ...` only applies to the
    field and not the array values.

For data structures, ensure you're using an `Immutable` variant or create a copy from the original source.
For example, prefer `ImmutableList`
from [kotlinx.collections.immutable](https://github.com/Kotlin/kotlinx.collections.immutable) over the standard `List`
type.
If that is not possible, use `myList.toList()` to create a copy before storing the list in an object.
