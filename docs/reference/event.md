# Event

## Overview

Events objects are immutable data structures like [Effects](effect.md) and [Models](model.md).
Events cause [Mobius loops](mobius-loop.md) to progress, only Events execute business logic and change the state.

Events allow Update functions to process new data to produce side effects or model changes.
Events are the only way to put new information in the [Update](update.md) function, which may result in model changes.
This means the only way to put new data in the Model is with an Event.

As with Model changes Effects can only be triggered by Events because the Update function determines
when Effects should occur.

Event names should be named with business logic terms instead of UI event terms
(ex: `LoginRequest` is a better name than `LoginButtonClicked`).
This is because there may be more than one way of triggering the same event from the UI.
In the `LoginRequest` case, this may be triggered by a button click or key press event.

!!! info "Not everything in the UI needs to be an Event"

    Animations for example do not always affect business logic, so no Events need to be associated with them.
    Intermediate animation events most likely don't need to be handled, but there may be an end event to trigger a screen transition.

## Event Types

There are the categories of `Events` based on the event source: interaction, effect feedback, external:

!!! info

    This distinction between event types is only useful when reasoning about `Events`.
    All events are treated the same in code, the `Update` function doesn't know anything about the source of the event.

### Interaction events

This is the main type of Event, they are considered “the public API” of a Mobius loop.
They are generally triggered by user interaction in the UI (Visual or Terminal based).
These Events will usually be actions or intentions rather than the intended effect.
For example: `UsernameInputChanged`, `ForgotPasswordClicked`, `SendPaymentRequested`.

### Effect feedback events

When Effect Handlers need to provide results to the loop, Effect feedback events are required.
For example: Calling a backend API could produce `DataLoaded` events on success or `DataLoadingFailed` on errors.

### External events

Events may occur on the platform without user interaction that need to be forwarded into the loop.
Some example of these events are: `NetworkStateChanged`, `DeviceDisconnected`, `PhoneCallStarted`.

## Guidelines for Events

- Seem more details for defining events at [Patterns > Events and Effects](../patterns/events-and-effects.md).
- Use names based on user intent in the past tense. For example: `LoginRequested`, `UsernameChanged`, etc.
