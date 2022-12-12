rootProject.name = "mobiuskt"

include(
        ":mobiuskt-core",
        ":mobiuskt-test",
        ":mobiuskt-extras",
        ":mobiuskt-coroutines",
        ":mobiuskt-autowire-generator",
        ":mobiuskt-autowire-api",
        ":mobiuskt-autowire-example",
        ":mobiuskt-update-generator",
        ":mobiuskt-update-generator-api",
        ":mobiuskt-update-generator-test",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
