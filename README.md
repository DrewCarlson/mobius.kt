# kotlin-mobius
[![CircleCI](https://circleci.com/gh/DrewCarlson/kotlin-mobius.svg?style=shield&circle-token=5969d260c8b600d8fb1e55b633eabb7760fcea46)](https://circleci.com/gh/DrewCarlson/kotlin-mobius)
[![Build status](https://ci.appveyor.com/api/projects/status/khk856p46q6ybqiy/branch/master?svg=true)](https://ci.appveyor.com/project/DrewCarlson/kotlin-mobius/branch/master)
[![Bintray](https://img.shields.io/bintray/v/drewcarlson/kotlin-mobius/core-common.svg?style=flat-rounded)](https://bintray.com/drewcarlson/kotlin-mobius)

An experimental port of [Mobius](https://github.com/spotify/mobius) to Kotlin, with [Multiplatform Project](https://kotlinlang.org/docs/reference/multiplatform.html) (MPP) support.

## What is Mobius?

The core construct provided by Mobius is the Mobius Loop, best described by the official documentation. _(Embedded below)_

A Mobius loop is a part of an application, usually including a user interface.
In a Spotify context, there is usually one loop per feature such as “the album page”, “login flow”, etc., but a loop can also be UI-less and for instance be tied to the lifecycle of an application or a user session.

### Mobius Loop

![Mobius Loop Diagram](https://raw.githubusercontent.com/wiki/spotify/mobius/mobius-diagram.png)

> A Mobius loop receives [Events](https://github.com/spotify/mobius/wiki/Event), which are passed to an [Update](https://github.com/spotify/mobius/wiki/Update) function together with the current [Model](https://github.com/spotify/mobius/wiki/Model).
> As a result of running the Update function, the Model might change, and [Effects](https://github.com/spotify/mobius/wiki/Effect) might get dispatched.
> The Model can be observed by the user interface, and the Effects are received and executed by an [Effect Handler](https://github.com/spotify/mobius/wiki/Effect-Handler).

'Pure' in the diagram refers to pure functions, functions whose output only depends on their inputs, and whose execution has no observable side effects.
 See [Pure vs Impure Functions](https://github.com/spotify/mobius/wiki/Pure-vs-Impure-Functions) for more details.

_(Source: [Concepts > Mobius Loop](https://github.com/spotify/mobius/wiki/Concepts/53777574e070e168f2c3bdebc1be544edfcee2cf#mobius-loop))_

By combining this concept with Kotlin's MPP features, kotlin-mobius allows you to write and test all of your pure functions (application and/or business logic) in Kotlin and deploy it everywhere.
This leaves impure functions to the native platform, which can be written in their primary language (Js, Java, Objective-c/Swift) or in Kotlin!

## Samples


### Todo

##### iOS

Open and run the `samples/todo/todo-ios` Xcode project, it contains a build step to produce the Kotlin framework.
Note the first build will take some time while it downloads all the necessary dependencies.

## Download

Artifacts are published to [bintray](https://bintray.com/drewcarlson/kotlin-mobius).

Note: All platform SourceSets can depend on the same artifact, the embedded metadata will be used to resolve the platform specific variant.

```groovy
dependencies {
  implementation 'com.github.DrewCarlson.kotlin-mobius:mobius-core:0.0.9'
  implementation 'com.github.DrewCarlson.kotlin-mobius:framework-android:0.0.9'
}
```
