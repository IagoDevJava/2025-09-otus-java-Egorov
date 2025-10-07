import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
    id("io.freefair.lombok")
}

dependencies {
    implementation("com.google.guava:guava")
    implementation("org.projectlombok:lombok")
    implementation("ch.qos.logback:logback-classic")
    implementation("org.slf4j:slf4j-api")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("gradleHelloWorld")
        archiveVersion.set("0.1")
        archiveClassifier.set("")
        manifest {
            attributes(mapOf("Main-Class" to "ru.otus.App"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
