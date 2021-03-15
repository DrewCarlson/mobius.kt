val isAndroidLibrary = plugins.hasPlugin("com.android.library")
if (isAndroidLibrary) {
    plugins.apply("digital.wup.android-maven-publish")
} else {
    plugins.apply("maven-publish")
}

apply(plugin = "signing")
apply(plugin = "org.jetbrains.dokka")

System.getenv("GITHUB_REF")?.let { ref ->
    if (ref.startsWith("refs/tags/")) {
        version = ref.substringAfterLast("refs/tags/")
    }
}

val mavenUrl: String by extra
val mavenSnapshotUrl: String by extra
val signingKey: String? by project
val signingPassword: String? by project
val sonatypeUsername: String? by project
val sonatypePassword: String? by project
val pomProjectUrl: String by project
val pomProjectDescription: String by project
val pomScmUrl: String by project
val pomDeveloperId: String by project
val pomDeveloperName: String by project
val pomLicenseName: String by project
val pomLicenseUrl: String by project
val pomLicenseDistribution: String by project

task<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}

if (tasks.none { it.name == "sourcesJar" }) {
    task<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        afterEvaluate {
            if (isAndroidLibrary) {
                //from(android.sourceSets["main"].java.srcDirs)
            } else {
                //from(sourceSets["main"].allSource)
            }
        }
    }
}

configure<PublishingExtension> {
    components.all {
        publications.withType<MavenPublication> {
            artifact(tasks.named("javadocJar"))
            artifact(tasks.named("sourcesJar"))
            if (isAndroidLibrary) {
                from(components.findByName("android"))
            } else {
                from(components.findByName("kotlin") ?: components.findByName("java"))
            }
            with(pom) {
                name.set(rootProject.name)
                url.set(pomProjectUrl)
                description.set(pomProjectDescription)
                scm {
                    url.set(pomScmUrl)
                }
                developers {
                    developer {
                        id.set(pomDeveloperId)
                        name.set(pomDeveloperName)
                    }
                }
                licenses {
                    license {
                        name.set(pomLicenseName)
                        url.set(pomLicenseUrl)
                        distribution.set(pomLicenseDistribution)
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri(mavenSnapshotUrl)
            } else {
                uri(mavenUrl)
            }
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

configure<SigningExtension> {
    isRequired = !version.toString().endsWith("SNAPSHOT")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign((extensions["publishing"] as PublishingExtension).publications)
}
