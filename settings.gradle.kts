rootProject.name = "mobiuskt"

enableFeaturePreview("GRADLE_METADATA")

include(
    ":mobius-core",
    ":mobius-extras",
    ":mobius-internal",
    ":jvm:mobius-android"
)

include(":mobiuskt-coroutines")

// Samples
//include ":samples:todo:todo-common"
//include ":samples:todo:todo-ios"
//include ":samples:todo:todo-web"
