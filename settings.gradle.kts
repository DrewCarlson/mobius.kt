rootProject.name = "mobiuskt"

include(
        ":mobiuskt-core",
        ":mobiuskt-test",
        ":mobiuskt-extras",
        ":mobiuskt-coroutines",
        ":mobiuskt-update-generator",
        ":mobiuskt-update-generator-api",
        ":mobiuskt-update-generator-test",
)

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
