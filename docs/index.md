# Mobius.kt

[![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/mobiuskt-core-jvm?label=maven&color=blue)](https://central.sonatype.com/search?q=mobiuskt-*&namespace=org.drewcarlson)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.drewcarlson/mobiuskt-core-jvm?server=https%3A%2F%2Fs01.oss.sonatype.org)
![](https://github.com/DrewCarlson/mobius.kt/workflows/Tests/badge.svg)
[![Codecov](https://img.shields.io/codecov/c/github/drewcarlson/mobius.kt?token=7DKJUD60BO)](https://app.codecov.io/gh/DrewCarlson/mobius.kt/)

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Js&color=blue)
![](https://img.shields.io/static/v1?label=&message=Wasm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Jvm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Linux&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=Windows&color=blue)
![](https://img.shields.io/static/v1?label=&message=iOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=tvOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=watchOS&color=blue)

Kotlin Multiplatform framework for managing state evolution and side-effects, based on [spotify/Mobius](https://github.com/spotify/mobius).

Features:

- Support for all Kotlin Multiplatform targets
- API surface usable from Kotlin, Java, Swift, and Javascript
- Coroutine and Flow support
- Testing utilities module
- Reduced boilerplate with code generation
- No runtime dependencies (*[atomicfu](https://github.com/Kotlin/kotlinx-atomicfu/) for Native targets)

**[Sample Project](https://github.com/DrewCarlson/pokedex-mobiuskt)**

**[Getting Started](https://drewcarlson.github.io/mobius.kt/v{{lib_version}}/getting-started/)**

**[Download](https://drewcarlson.github.io/mobius.kt/v{{lib_version}}/download/)**

**[API Docs](https://drewcarlson.github.io/mobius.kt/v{{lib_version}}/kdoc/)**

## What is Mobius?

The core construct provided by Mobius is the [Mobius Loop](reference/mobius-loop.md), best described by the official
documentation. _(Embedded below)_

A Mobius loop is a part of an application, usually including a user interface.
In a Spotify context, there is usually one loop per feature such as “the album page”, “login flow”, etc., but a loop can also be UI-less and for instance be tied to the lifecycle of an application or a user session.


### Mobius Loop

![Mobius Loop Diagram](https://raw.githubusercontent.com/wiki/spotify/mobius/mobius-diagram.png)

> A Mobius loop receives [Events](reference/event.md), which are passed to an [Update](reference/update.md) function together with the current [Model](reference/model.md).
> As a result of running the Update function, the Model might change, and [Effects](reference/effect.md) might get dispatched.
> The Model can be observed by the user interface, and the Effects are received and executed by an [Effect Handler](reference/effect-handler.md).

'Pure' in the diagram refers to pure functions, functions whose output only depends on their inputs, and whose execution has no observable side effects.
See [Pure vs Impure Functions](patterns/pure-vs-impure-functions.md) for more details.

_(Source: [Spotify/Mobius](https://github.com/spotify/mobius/) - [Concepts > Mobius Loop](https://spotify.github.io/mobius/concepts/#mobius-loop))_

## Why Mobius.kt

By combining Mobius Loops with Kotlin's [Multiplatform](https://kotlinlang.org/docs/multiplatform.html) features, mobius.kt allows you to write and test Update functions in platform-independent (common) Kotlin and deploy them everywhere.
This leaves impure functions to be written in multiplatform Kotlin code or the target platform's primary language (Js, Java, Objective-c/Swift), depending on your use-case.
