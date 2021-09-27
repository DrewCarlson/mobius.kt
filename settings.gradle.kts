rootProject.name = "mobiuskt"

include(
    ":mobiuskt-core",
    ":mobiuskt-extras",
    ":mobiuskt-internal",
    ":jvm:mobiuskt-android"
)

include(":mobiuskt-coroutines")

// Samples
//include ":samples:todo:todo-common"
//include ":samples:todo:todo-ios"
//include ":samples:todo:todo-web"
