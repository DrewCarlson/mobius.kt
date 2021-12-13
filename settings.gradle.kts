rootProject.name = "mobiuskt"

include(
        ":mobiuskt-core",
        ":mobiuskt-extras",
        ":mobiuskt-android",
        ":mobiuskt-coroutines",
)

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
