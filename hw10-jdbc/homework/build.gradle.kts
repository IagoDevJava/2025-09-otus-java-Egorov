dependencies {
    implementation(project(":hw10-jdbc:demo"))

    implementation("ch.qos.logback:logback-classic")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql")
}

tasks.register<JavaExec>("runHomeWork") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ru.otus.HomeWork")
}