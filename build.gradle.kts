plugins {
    id("java")
    application
}

group = "com.back"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "com.back.Main"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
