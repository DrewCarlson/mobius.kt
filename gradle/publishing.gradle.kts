apply(plugin = "maven-publish")

val mavenUrl: String by extra
val mavenSnapshotUrl: String by extra

configure<PublishingExtension> {
    repositories {
        maven {
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri(mavenSnapshotUrl)
            } else {
                uri(mavenUrl)
            }
            credentials {
                username = System.getenv("BINTRAY_USER")
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}
