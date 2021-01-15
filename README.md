# mobius.kt

![Bintray](https://img.shields.io/bintray/v/drewcarlson/mobius.kt/mobius-core?color=blue)
![](https://img.shields.io/maven-metadata/v?label=artifactory&logoColor=lightgrey&metadataUrl=https%3A%2F%2Foss.jfrog.org%2Fartifactory%2Foss-snapshot-local%2Fkt%2Fmobius%2Fmobius-core%2Fmaven-metadata.xml&color=lightgrey)
![](https://github.com/DrewCarlson/mobius.kt/workflows/Jvm/badge.svg)
![](https://github.com/DrewCarlson/mobius.kt/workflows/Native/badge.svg)

Multiplatform Kotlin [Mobius](https://github.com/spotify/mobius) implementation.

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

By combining this concept with Kotlin's MPP features, mobius.kt allows you to write and test all of your pure functions (application and/or business logic) in Kotlin and deploy it everywhere.
This leaves impure functions to the native platform, which can be written in their primary language (Js, Java, Objective-c/Swift) or in Kotlin!

## Samples


### Todo

##### iOS

Open and run the `samples/todo/todo-ios` Xcode project, it contains a build step to produce the Kotlin framework.
Note the first build will take some time while it downloads all the necessary dependencies.

## Download

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Js&color=blue)
![](https://img.shields.io/static/v1?label=&message=Jvm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Linux&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=Windows&color=blue)
![](https://img.shields.io/static/v1?label=&message=iOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=tvOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=watchOS&color=blue)

Artifacts are available on [bintray](https://bintray.com/drewcarlson/mobius.kt).

![Bintray](https://img.shields.io/badge/dynamic/json.svg?label=Bintray&query=name&style=flat&url=https%3A%2F%2Fbintray.com%2Fapi%2Fv1%2Fpackages%2Fdrewcarlson%2Fmobius.kt%2Fmobius-core%2Fversions%2F_latest)

```kotlin
repositories {
    jcenter()
    // Or snapshots
    maven { setUrl("http://oss.jfrog.org/artifactory/oss-snapshot-local") }
}
dependencies {
    implementation 'kt.mobius:mobius-core:VERSION'
    implementation 'kt.mobius:mobius-extras:VERSION'
   implementation 'kt.mobius:mobius-android:VERSION'
}
```
