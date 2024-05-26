rootProject.name = "mobiuskt"

include(
        ":mobiuskt-core",
        ":mobiuskt-test",
        ":mobiuskt-extras",
        ":mobiuskt-coroutines",
        ":mobiuskt-codegen",
        ":mobiuskt-codegen-api",
        ":mobiuskt-compose",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
