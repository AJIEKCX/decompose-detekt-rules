import org.jreleaser.model.Active

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jreleaser)
    `maven-publish`
}

group = properties["GROUP"].toString()
version = properties["VERSION_NAME"].toString()
description = "Detekt ruleset for Decompose project"

dependencies {
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.test)
    testImplementation(libs.kotest)
    testImplementation(libs.jupiter)
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    systemProperty("compile-snippet-tests", project.hasProperty("compile-test-snippets"))
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])

            groupId = properties["GROUP"].toString()
            artifactId = "decompose-detekt-rules"

            pom {
                name.set(project.properties["POM_NAME"].toString())
                description.set(project.description)
                url.set("https://github.com/AJIEKCX/decompose-detekt-rules")
                issueManagement {
                    url.set("https://github.com/AJIEKCX/decompose-detekt-rules/issues")
                }

                scm {
                    url.set("https://github.com/AJIEKCX/decompose-detekt-rules")
                    connection.set("scm:git://github.com/AJIEKCX/decompose-detekt-rules.git")
                    developerConnection.set("scm:git://github.com/AJIEKCX/decompose-detekt-rules.git")
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("AJIEKCX")
                        name.set("Alexey Panov")
                        email.set("panovalexsey@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        inceptionYear = "2025"
        author("@AJIEKCX")
    }
    release {
        github {
            skipRelease = true
            skipTag = true
            sign = true
            branch = "main"
            branchPush = "main"
            overwrite = true
        }
    }
    signing {
        active = Active.ALWAYS
        armored = true
        verify = true
    }
    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                setAuthorization("Basic")
                retryDelay = 60
            }
        }
    }
}
