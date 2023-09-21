# Model

## Overview

The Model is an [immutable](../patterns/immutability.md) representation of the internal state and configuration of
a [Mobius loop](mobius-loop.md).
It contains two kinds of data, often overlapping: what you need to make business logic decisions,
and what you need to present in the UI.
[Update](update.md) functions can only consider the current Model and the incoming [Event](event.md), so data required
to produce a result must be stored there.

Since the Model is immutable, a new instance will need to be created whenever it needs to change.
Because everything is immutable, only a shallow copy is needed so references to a previous model can be shared safely.

Resist the temptation to put configuration or state as member fields in your Update function, it will mean that the
Update function is not pure.
This would make it difficult to reason about the loop in the same way.

When starting a Mobius loop, a Model instance is required to start from.
This can be the initial state of the loop or a previous Model you want to resume execution from.
Since the Update function doesn't have any memory besides the model, all valid Models can be used to start a new loop.

## Guidelines for Models

**The Model and Event must contain all data needed in the Update function**

The ensures all data used to make decisions must be in the Model, you should not use state from anywhere else in the
loop.

**The Model should contain all deterministic UI state.**

Models will inevitably hold UI specific state, but when overdone changes to the UI will be harder to understand and
test than Model changes.
Simpler UI will produce a more robust loop.

**Avoid UI concerns in the Model.**

The Model should primarily be focused on making decisions and not about rendering.
Since the Model is an input for rendering, there will be some coupling, but it will be helpful to define the Model
in terms of the meaning behind something rather than the representation.

For example, prefer a name like `canLogin` instead of `isLoginButtonEnabled`.
This should be your default strategy for model field naming, but it is sometimes useful for the loop to have UI
concerns.
In such cases you should determine the best name given your context as UI concerns may be desired/unavoidable,
so it is best not to hide it.

**The Model holds all configuration.**

In the context, Configuration could mean the user id for loading a specific user profile,
or what A/B-test group the user is in.

!!! note "A/B-test naming"

    In the case of an A/B-test flag that changes behavior, it should be expressed as the behavior that is changed,
    not in terms of what the test is.
    For example, prefer `shouldShowMutalFriends` vs `isUserInMutualFriendsTestGroup`.
    Translating from A/B-flags to configuration should be done when instantiating your model

**Do not put behavior in the Model.**

The Model should be considered a value object.
It is okay to put simple helper methods in the Model to make it easier to create new versions of it,
but avoid making decisions in it.
The Model in Mobius is just an object that holds some data, and shouldn’t be compared to the “model” of MVP or MVC,
where it usually also contains domain logic.
