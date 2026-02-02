rootProject.name = "2025-09-otus-java-Egorov"
include("hw01-gradle")
include("hw02-collections")
include("hw03-tests")
include("hw05-byte-code")
include("hw06-gc")
include("hw07-ATM")

pluginManagement {
    val jgitver: String by settings
    val dependencyManagement: String by settings
    val springframeworkBoot: String by settings
    val johnrengelmanShadow: String by settings
    val jib: String by settings
    val protobufVer: String by settings
    val sonarlint: String by settings
    val spotless: String by settings
    val lombokPluginVersion: String by settings

    plugins {
        id("fr.brouillard.oss.gradle.jgitver") version jgitver
        id("io.spring.dependency-management") version dependencyManagement
        id("org.springframework.boot") version springframeworkBoot
        id("com.github.johnrengelman.shadow") version johnrengelmanShadow
        id("com.google.cloud.tools.jib") version jib
        id("com.google.protobuf") version protobufVer
        id("name.remal.sonarlint") version sonarlint
        id("com.diffplug.spotless") version spotless
        id("io.freefair.lombok") version lombokPluginVersion
    }
}

include("hw06-gc:homework")
findProject(":hw06-gc:homework")?.name = "homework"
include("hw08-patterns")
include("hw09-io")
include("hw10-jdbc")
include("hw10-jdbc:demo")
findProject(":hw10-jdbc:demo")?.name = "demo"
include("hw10-jdbc:homework")
findProject(":hw10-jdbc:homework")?.name = "homework"
include("hw11-hibernate")
include("hw12-cache")
