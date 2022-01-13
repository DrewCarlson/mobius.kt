rootProject.name = "mobiuskt"

include(
        ":mobiuskt-core",
        ":mobiuskt-test",
        ":mobiuskt-extras",
        ":mobiuskt-android",
        ":mobiuskt-coroutines",
        ":mobiuskt-update-spec",
        ":mobiuskt-update-spec-api",
        ":mobiuskt-update-spec-test",
)

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
