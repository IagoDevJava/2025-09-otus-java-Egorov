plugins {
    id("java")
}

group = "com.company.testframework"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.42")
    implementation("ch.qos.logback:logback-classic")
}

tasks.register<JavaExec>("runExample") {
    group = "application"
    description = "Run the example test"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.company.testframework.example.ExampleTest"
}